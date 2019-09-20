package me.timothy.bots;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.json.simple.parser.ParseException;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.diagnostics.Diagnostics;
import me.timothy.bots.models.BannedUser;
import me.timothy.bots.models.DelayedVettingRequest;
import me.timothy.bots.models.LendersCampContributor;
import me.timothy.bots.models.PromotionBlacklist;
import me.timothy.bots.models.Recheck;
import me.timothy.bots.models.ResetPasswordRequest;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.redflags.RedFlagsDriver;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.summon.CommentSummon;
import me.timothy.bots.summon.LinkSummon;
import me.timothy.bots.summon.PMSummon;
import me.timothy.bots.summon.RedFlagSummon;
import me.timothy.bots.summon.VettedSummon;
import me.timothy.jreddit.RedditUtils;
import me.timothy.jreddit.info.Account;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.ContributorsListing;
import me.timothy.jreddit.info.Link;
import me.timothy.jreddit.info.Listing;
import me.timothy.jreddit.info.Message;
import me.timothy.jreddit.info.Thing;

/**
 * The bot driver for the loans bot
 * 
 * @author Timothy
 */
public class LoansBotDriver extends BotDriver {
	private static final int MAX_RECENTLY_CHECKED = 50;
	private static final long FORCE_RECHECK_TIME_MS = 1000 * 60 * 60 * 24;
	private static final boolean TEST_SERVER = false;
	
	/**
	 * Describes a very simply mapping of the username and time
	 * it was checked, for canInteractWithUsFull, to prevent 
	 * excessive checking of power users karma. 
	 * 
	 * @author Timothy
	 *
	 */
	private static class RequirementsCheckedUser {
		public String username;
		public long timeCheckedMS;
		
		public RequirementsCheckedUser(String username, long timeChecked) {
			this.username = username;
			this.timeCheckedMS = timeChecked;
		}
	}
	
	/**
	 * The list of users that have recently passed as full
	 * canInteractWithUsFull
	 */
	private List<RequirementsCheckedUser> recentlyPassedCheckUsers;
	
	private Diagnostics diagnostics;
	
	private RedFlagsDriver redFlagsDriver;
	
	/**
	 * Exact echo of BotDriver constructor; initializes diagnostics
	 * @param database database
	 * @param config config
	 * @param bot the bot
	 * @param commentSummons comment summons
	 * @param pmSummons pm summons
	 * @param submissionSummons submission summons
	 */
	public LoansBotDriver(Database database, FileConfiguration config, Bot bot,
			CommentSummon[] commentSummons, PMSummon[] pmSummons,
			LinkSummon[] submissionSummons) {
		super(database, config, bot, commentSummons, pmSummons, submissionSummons);
		
		((VettedSummon)pmSummons[0]).inviter = (user) -> {
			new Retryable<Boolean>("Invite to lenders camp", maybeLoginAgainRunnable){
				@Override
				protected Boolean runImpl() throws Exception {
					RedditUtils.addContributor("lenderscamp", user.username, bot.getUser());
					return true;
				}
			}.run();
		};
		
		diagnostics = new Diagnostics(new File("diagnostics.log"));
		
		recentlyPassedCheckUsers = new ArrayList<RequirementsCheckedUser>();
		
		redFlagsDriver = new RedFlagsDriver((LoansDatabase)database, (LoansFileConfiguration)config, bot, maybeLoginAgainRunnable, 2000);
		for(LinkSummon l : submissionSummons) {
			if(l instanceof RedFlagSummon) {
				((RedFlagSummon)l).setDriver(redFlagsDriver);
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see me.timothy.bots.BotDriver#handleReply(me.timothy.jreddit.info.Thing, java.lang.String)
	 */
	@Override
	protected void handleReply(Thing replyable, String response) {
		String subreddit = null;
		if(replyable instanceof Comment) {
			subreddit = ((Comment) replyable).subreddit();
		}else if(replyable instanceof Link) {
			subreddit = ((Link) replyable).subreddit();
		}

		if(subreddit != null) {
			if(LoansBotUtils.SECONDARY_SUBREDDITS.contains(subreddit.toLowerCase())) {
				if(!response.endsWith("\n\n")) {
					if(response.endsWith("\n")) {
						response = response + "\n";
					}else {
						response = response + "\n\n";
					}

					String postfix = ((LoansDatabase) database).getResponseMapping().fetchByName("secondary_subreddit_postfix").responseBody;
					postfix = postfix.replace("<subreddit>", subreddit);
					postfix = postfix.replace("<primary>", LoansBotUtils.PRIMARY_SUBREDDIT);

					response = response + postfix;
				}
			}
		}
		super.handleReply(replyable, response);
	}
	
	

	/* (non-Javadoc)
	 * @see me.timothy.bots.BotDriver#doLoop()
	 */
	@Override
	protected void doLoop() throws IOException, ParseException,
	java.text.ParseException {
		logger.debug("Scanning for new claim codes..");
		handleClaimCodes();
		sleepFor(BRIEF_PAUSE_MS);
		
		if(!TEST_SERVER) {
			logger.debug("Scanning for recheck requests..");
			handleRechecks();
			sleepFor(BRIEF_PAUSE_MS);
		}
		
		logger.debug("Scanning for reset password requests..");
		handleResetPasswordRequests();
		sleepFor(BRIEF_PAUSE_MS);
		
		if(!TEST_SERVER) {
			logger.debug("Scanning for new lenders camp contributors..");
			handleLendersCampContributors();
			sleepFor(BRIEF_PAUSE_MS);
		}
		
		logger.debug("Performing self-assessment...");
		handleDiagnostics();
		
		logger.debug("Pruning recent posts table...");
		pruneRecentPosts();
		
		logger.debug("Running the red flags driver...");
		redFlagsDriver.handleQueue(21);
		
		logger.debug("Pruning old failed login attempts...");
		((LoansDatabase)database).getFailedLoginAttemptMapping().prune();
		
		if(!TEST_SERVER) {
			super.doLoop();
		}else {
			logger.trace("Considering relogging in..");
			maybeLoginAgain();
		}
	}

	/**
	 * Loops through unclaimed users who have a claim code but no claim
	 * link sent at and sends them a reddit PM with the claim code, then updates
	 * the claim link sent at to now.
	 */
	private void handleClaimCodes() {
		LoansDatabase ldb = (LoansDatabase) database;
		List<User> toSendCode = ldb.getUserMapping().fetchUsersToSendCode();
		
		for(User user : toSendCode) {
			List<Username> usernames = ldb.getUsernameMapping().fetchByUserId(user.id);
			
			for(Username username : usernames) {
				logger.info("Sending claim code to " + username.username);
				
				String message = ((LoansDatabase) database).getResponseMapping().fetchByName("claim_code").responseBody;
				message = message.replace("<user>", username.username);
				message = message.replace("<code>", user.claimCode);
				message = message.replace("<codeurl>", "https://redditloans.com/claim.php?username=" + username.username + "&user_id=" + user.id + "&claim_code=" + user.claimCode);
				sendMessage(username.username, "RedditLoans Account Claimed", message);
				
				user.claimLinkSentAt = new Timestamp(System.currentTimeMillis());
				ldb.getUserMapping().save(user);
				sleepFor(BRIEF_PAUSE_MS);
			}
		}
	}
	
	/**
	 * Loops through all queued rechecks and handles
	 * them, then removes them from the queue
	 */
	private void handleRechecks() {
		boolean silentMode = Boolean.valueOf(config.getProperty("rechecks.silent_mode"));
		LoansDatabase ldb = (LoansDatabase) database;
		
		List<Recheck> rechecks = ldb.getRecheckMapping().fetchAll();
		if(rechecks.size() == 0)
			return;
		
		List<Recheck[]> batches = new ArrayList<Recheck[]>();
		
		while(rechecks.size() > 5 && batches.size() < 5) {
			Recheck[] batch = new Recheck[5];
			for(int i = 0; i < 5; i++) {
				batch[i] = rechecks.get(0);
				rechecks.remove(0);
			}
			batches.add(batch);
		}
		
		if(rechecks.size() <= 5) {
			Recheck[] lastBatch = rechecks.toArray(new Recheck[0]);
			batches.add(lastBatch);
		}
		
		logger.info("Performing " + batches.size() + " batches of rechecks");
		
		for(Recheck[] batch : batches) {
			final String[] asStr = new String[batch.length];
			for(int i = 0; i < batch.length; i++) {
				asStr[i] = batch[i].fullname;
				ldb.getRecheckMapping().delete(batch[i]);
			}
			
			Listing listing = new Retryable<Listing>("Get things for rechecks", maybeLoginAgainRunnable) {
				@Override
				protected Listing runImpl() throws Exception {
					return RedditUtils.getThings(asStr, bot.getUser());
				}
			}.run();
			logger.trace(String.format("Batch size %d got %d things", batch.length, listing.numChildren()));
			for(int i = 0; i < listing.numChildren(); i++) {
				Thing thing = listing.getChild(i);
				
				handleRecheck(thing, silentMode);
			}
		}
	}

	/**
	 * Handles a particular recheck by determining its type
	 * and calling the appropriate function in BotDriver
	 * @param thing the thing to recheck
	 * @param silentMode if the bot is in silent mode for rechecks
	 */
	private void handleRecheck(Thing thing, boolean silentMode) {
		if(database.containsFullname(thing.fullname())) {
			logger.trace(String.format("Skipping %s because the database contains it", thing.fullname()));
			return;
		}
		
		if(thing instanceof Comment) {
			Comment comment = (Comment) thing;
			
			final String linkId = comment.linkID();
			Listing listing = new Retryable<Listing>("Get things for rechecks", maybeLoginAgainRunnable) {
				@Override
				protected Listing runImpl() throws Exception {
					return RedditUtils.getThings(new String[] { linkId }, bot.getUser());
				}
			}.run();
			sleepFor(BRIEF_PAUSE_MS);
			if(listing.numChildren() != 1) {
				logger.warn("Couldn't find link author for comment " + comment.fullname());
			}else {
				Link link = (Link) listing.getChild(0);
				comment.linkAuthor(link.author());
				comment.linkURL(link.url());
			}
				
			handleComment(comment, true, silentMode);
		}else if(thing instanceof Link) {
			final Link link = (Link) thing;
			handleSubmission(link, silentMode);
			
			Listing replies = new Retryable<Listing>("Get link replies for link recheck", maybeLoginAgainRunnable) {
				@Override
				protected Listing runImpl() throws Exception {
					return RedditUtils.getLinkReplies(bot.getUser(), link.id());
				}
			}.run();
			sleepFor(BRIEF_PAUSE_MS);
			
			List<Comment> commentsToLookAt = new ArrayList<>();
			for(int i = 0; i < replies.numChildren(); i++) {
				Thing childThing = replies.getChild(i);
				if(childThing instanceof Comment) {
					if(childThing.fullname() != null) {
						commentsToLookAt.add((Comment) childThing);
					}else {
						logger.trace("(queueing comments of link to recheck) Null fullname for child thing: " + childThing);
					}
				}else {
					logger.trace("(queueing comments of link to recheck) Weird child thing: " + childThing);
				}
			}
			logger.trace(String.format("Found %d comments to recheck in %s", commentsToLookAt.size(), link.fullname()));
			
			for(Comment com : commentsToLookAt) {
				com.linkAuthor(link.author());
				com.linkURL(link.url());
				handleComment(com, true, silentMode);
			}
		}else if(thing instanceof Message) {
			handlePM(thing, silentMode);
		}
	}
	/**
	 * Sends reset password links out to reddit users who requested
	 * a reset password link
	 */
	private void handleResetPasswordRequests() {
		LoansDatabase db = ((LoansDatabase) database);
		List<ResetPasswordRequest> resetPasswordRequests = db.getResetPasswordRequestMapping().fetchUnsent();
		
		if(resetPasswordRequests.size() == 0) {
			return;
		}
		
		logger.debug(String.format("There are %d pending reset password requests", resetPasswordRequests.size()));
		
		boolean first = true;
		for(ResetPasswordRequest rpr : resetPasswordRequests) {
			if(!first)
				sleepFor(BRIEF_PAUSE_MS);
			else
				first = false;
			
			User user = db.getUserMapping().fetchById(rpr.userId);
			if(user == null) {
				logger.warn(String.format("Reset Password Request id=%d has user_id=%d, which is not correlated with any user", rpr.id, rpr.userId));
				continue;
			}
			
			Response resp = db.getResponseMapping().fetchByName("reset_password");
			ResponseInfo rInfo = new ResponseInfo();
			rInfo.addTemporaryString("userid", Integer.toString(user.id));
			rInfo.addTemporaryString("code", rpr.resetCode);
			
			String message = new ResponseFormatter(resp.responseBody, rInfo).getFormattedResponse(config, db);
			
			List<Username> usernames = db.getUsernameMapping().fetchByUserId(user.id);
			for(Username username : usernames) {
				logger.info(String.format("Sending reset password code to %s", username.username));
				sendMessage(username.username, "RedditLoans Reset Password", message);
				
				rpr.resetCodeSent = true;
				db.getResetPasswordRequestMapping().save(rpr);
			}
		}
	}

	/**
	 * Checks reddits list of lenders camp contributors and verifies we
	 * aren't missing any
	 */
	private void updateLendersCampContributors() {
		LoansDatabase ldb = (LoansDatabase) database;
		
		Listing contribs = new Retryable<Listing>("Get lenderscamp contributors", maybeLoginAgainRunnable){
			@Override
			protected Listing runImpl() throws Exception {
				return RedditUtils.getContributorsForSubreddit("lenderscamp", bot.getUser());
			}
		}.run();
		sleepFor(BRIEF_PAUSE_MS);
		
		for(int i = 0; i < contribs.numChildren(); i++) {
			Account contribAcc = (Account) contribs.getChild(i);
			String usernameStr = contribAcc.name();
			
			Username username = ldb.getUsernameMapping().fetchByUsername(usernameStr);
			if(username != null) {
				if(!ldb.getLccMapping().contains(username.userId)) {
					logger.info(String.format("Detected outside person added contributor %s to lenderscamp", usernameStr));
					long now = System.currentTimeMillis();
					LendersCampContributor lcc = new LendersCampContributor(-1, username.userId, false, new Timestamp(now), new Timestamp(now));
					ldb.getLccMapping().save(lcc);
				}
			}
		}
	}
	
	/**
	 * Handles lenders camp contributors
	 */
	private void handleLendersCampContributors() {
		final long UTC_TO_GMT_MILLIS = -8 * 60 * 60 * 1000; 
		updateLendersCampContributors();
		
		LoansDatabase ldb = (LoansDatabase) database;
		// This can take an extremely long time to catch up - lets make sure it doesn't lose its
		// progress on restarts
		Properties defaultProps = new Properties();
		defaultProps.setProperty("id_next", "1");
		defaultProps.setProperty("last_check", "-1");
		Properties lccProgressProps = new Properties(defaultProps);
		File checkFile = new File("lenders_camp_contributors_progress.properties");
		if(checkFile.exists()) {
			try(FileReader fr = new FileReader(checkFile)) {
				lccProgressProps.load(fr);
			}catch(IOException ex) {
				logger.catching(ex);
			}
		}
		int idNext = Integer.valueOf(lccProgressProps.getProperty("id_next"));
		long lastCheck = Long.valueOf(lccProgressProps.getProperty("last_check"));
		long theTime = new Date().getTime();
		
		List<Integer> userIdsToCheck = new ArrayList<>();
		if(lastCheck > 0) {
			Timestamp timestamp = new Timestamp(lastCheck + UTC_TO_GMT_MILLIS);
			userIdsToCheck.addAll(ldb.getLoanMapping().fetchLenderIdsWithNewLoanSince(timestamp));
		}
		
		int numAdditional = 10 - userIdsToCheck.size();
		for(int i = 0; i < numAdditional; i++) {
			if(idNext > ldb.getUserMapping().fetchMaxUserId())
				break;
			userIdsToCheck.add(idNext);
			idNext++;
		}
		
		String bodyFormatStd = ldb.getResponseMapping().fetchByName("vet_user_initial_pm_body").responseBody;
		String bodyFormatDvr = ldb.getResponseMapping().fetchByName("vet_user_delayed_pm_body").responseBody;
		
		DateFormat dateFmt = DateFormat.getDateTimeInstance();
		
		int reqNumComplAsLender = Integer.parseInt(config.getProperty("lenders_camp.num_completed_as_lender"));
		int reqNumStartedAsLender = Integer.parseInt(config.getProperty("lenders_camp.num_started_as_lender"));
		int reqMSSinceOldestPaid = Integer.parseInt(config.getProperty("lenders_camp.ms_since_oldest_paid"));
		
		// can't be bot since bot is this.bot
		User _bot = ldb.getUserMapping().fetchOrCreateByName(config.getProperty("user.username"));
		
		for(Integer userToCheckId : userIdsToCheck) {
			User userToCheck = ldb.getUserMapping().fetchById(userToCheckId);
			if(userToCheck == null)
				continue;
			
			List<Username> usernames = ldb.getUsernameMapping().fetchByUserId(userToCheck.id);
			int[] numLoansInfo = ldb.getLoanMapping().fetchNumberOfLoansCompletedWithUserAsLender(userToCheck.id);
			long timeSinceOldest = ldb.getLoanMapping().fetchTimeSinceEarliestRepaidLoan(userToCheck.id);
			int numberOfLoansAsLender = numLoansInfo[0];
			int numberCompletedAsLender = numLoansInfo[1];
			
			DelayedVettingRequest dvr = null;
			if(!ldb.getPromotionBlacklistMapping().fetchAllById(userToCheck.id).isEmpty()) {
				dvr = ldb.getDelayedVettingRequestMapping().fetchByUserId(userToCheck.id);
				if(dvr == null || dvr.numberLoans > numberOfLoansAsLender)
					continue;
			}
			
			if(numberCompletedAsLender >= reqNumComplAsLender && 
					numberOfLoansAsLender >= reqNumStartedAsLender &&
					timeSinceOldest > reqMSSinceOldestPaid &&
					!ldb.getLccMapping().contains(userToCheck.id) && 
					(dvr != null || !ldb.getPromotionBlacklistMapping().contains(userToCheck.id))) {
				logger.info(String.format("Asking mods to vet user %d (%s) (%d loans as lender, %d completed, %s ms since oldest $paid)", userToCheck.id, usernames.get(0).username, numberOfLoansAsLender, numberCompletedAsLender, timeSinceOldest));
				if(dvr != null) {
					dvr.rerequestedAt = new Timestamp(System.currentTimeMillis());
					ldb.getDelayedVettingRequestMapping().save(dvr);
				}else {
					ldb.getPromotionBlacklistMapping().save(new PromotionBlacklist(-1, userToCheck.id, _bot.id, "Vetting required", 
							new Timestamp(System.currentTimeMillis()), null));
				}
				for(final Username username : usernames) {
					Timestamp timeOfOldest = new Timestamp(System.currentTimeMillis() - timeSinceOldest);
					
					ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
					respInfo.addTemporaryString("username", username.username);
					respInfo.addTemporaryString("num completed as lender", Integer.toString(numberOfLoansAsLender));
					respInfo.addTemporaryString("num actually completed as lender", Integer.toString(numberCompletedAsLender));
					respInfo.addTemporaryString("date of oldest paid", dateFmt.format(timeOfOldest));
					String body = null;
					if(dvr == null) {
						body = new ResponseFormatter(bodyFormatStd, respInfo).getFormattedResponse(config, ldb);
					}else {
						respInfo.addTemporaryString("delayed_at", SimpleDateFormat.getDateTimeInstance().format(dvr.createdAt));
						respInfo.addTemporaryString("delayed_reason", dvr.reason);
						body = new ResponseFormatter(bodyFormatDvr, respInfo).getFormattedResponse(config, ldb);
					}
					
					final String bodyCp = body;
					new Retryable<Boolean>("Send vet request", maybeLoginAgainRunnable) {
						@Override
						protected Boolean runImpl() throws Exception {
							RedditUtils.sendPersonalMessage(bot.getUser(), "/r/borrow", "Vetting Required: /u/" + username.username, bodyCp);
							return Boolean.TRUE;
						}
					}.run();
					sleepFor(BRIEF_PAUSE_MS);
				}
			}
		}
		
		lccProgressProps.setProperty("id_next", String.valueOf(idNext));
		lccProgressProps.setProperty("last_check", String.valueOf(theTime));
		try(BufferedWriter fw = new BufferedWriter(new FileWriter(checkFile))) {
			lccProgressProps.store(fw, "Lenders Camp Contributors Progress Information");
		}catch(IOException ex) {
			logger.catching(ex);
		}
	}
	
	
	/**
	 * Handles diagnosing the diagnostics 
	 */
	private void handleDiagnostics() {
		diagnostics.diagnose();
	}
	
	/**
	 * Prune recent posts table
	 */
	private void pruneRecentPosts() {
		LoansDatabase ldb = (LoansDatabase) database;
		ldb.getRecentPostMapping().deleteOldEntries();
	}
	
	@Override
	protected boolean canInteractWithUsFull(final String username) {
		if(!super.canInteractWithUsFull(username))
			return false;
		
		for(int i = recentlyPassedCheckUsers.size() - 1; i >= 0; i--) {
			RequirementsCheckedUser checked = recentlyPassedCheckUsers.get(i);
			if(checked.username.equals(username))
			{
				long timeSinceChecked = System.currentTimeMillis() - checked.timeCheckedMS;
				
				if(timeSinceChecked >= FORCE_RECHECK_TIME_MS)
				{
					recentlyPassedCheckUsers.remove(i);
					break;
				}else {
					return true;
				}
			}else {
				// prune while we're here
				long timeSinceChecked = System.currentTimeMillis() - checked.timeCheckedMS;
				
				if(timeSinceChecked >= FORCE_RECHECK_TIME_MS) {
					recentlyPassedCheckUsers.remove(i);
				}
			}
		}
		
		logger.debug(String.format("%s has not been recently checked for interaction requirements, so fetching acount information..", username));
		Boolean meetsRequirements = new Retryable<Boolean>("meets interaction requirements") {

			@Override
			protected Boolean runImpl() throws Exception {
				Account account = RedditUtils.getAccountFor(bot.getUser(), username);
				sleepFor(BRIEF_PAUSE_MS);
				
				if(account == null) {
					logger.debug(String.format("Got not account information for %s, assuming he fails the test", username));
					return false;
				}
				
				if(account.commentKarma() + account.linkKarma() < 1000) {
					logger.debug(String.format("%s has %d comment karma and %d link karma, for a cumulative karma of %d, which is below minimum threshold - ignoring", username, account.commentKarma(), account.linkKarma(), account.commentKarma() + account.linkKarma()));
					return false;
				}
				
				double nowUTCSeconds = System.currentTimeMillis() / 1000.0;
				double minAge = nowUTCSeconds - 60 * 60 * 24 * 90;
				
				if(account.createdUTC() > minAge) {
					long timeInMS = (Math.round(minAge) * 1000);
					SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm aaa");
					String createdAt = sdf.format(new Date(timeInMS));
					logger.debug(String.format("%s created his account around %s, which does not meet the 90 day requirement", username, createdAt));
					return false;
				}
				
				return true;
			}
			
		}.run();
		
		if(meetsRequirements != Boolean.TRUE) {
			logger.debug(String.format("%s did not meet the interaction requirements. Checking if approved submitter", username));
			meetsRequirements = new Retryable<Boolean>("is contributor to borrow - interact reqs") {

				@Override
				protected Boolean runImpl() throws Exception {
					ContributorsListing contrib = RedditUtils.getContributorsForSubredditByName("borrow", username, bot.getUser());
					sleepFor(BRIEF_PAUSE_MS);
					
					if(contrib == null || contrib.numChildren() != 1) {
						logger.debug(String.format("%s is not an approved submitter to /r/borrow", username));
						return false;
					}
					
					logger.debug(String.format("%s is an approved submitter to /r/borrow", username));
					return true;
				}
				
			}.run();
		}
		
		if(meetsRequirements == null)
			return false;
		
		boolean result = meetsRequirements.booleanValue();
		if(result) {
			if(recentlyPassedCheckUsers.size() == MAX_RECENTLY_CHECKED) {
				boolean removedOne = false;
				int oldestIndex = -1;
				long oldestTime = 0;
				
				for(int i = recentlyPassedCheckUsers.size() - 1; i >= 0; i--) {
					RequirementsCheckedUser checked = recentlyPassedCheckUsers.get(i);

					long timeSinceChecked = System.currentTimeMillis() - checked.timeCheckedMS;
					if(timeSinceChecked >= FORCE_RECHECK_TIME_MS) {
						recentlyPassedCheckUsers.remove(i);
						removedOne = true;
					}
					
					if(!removedOne) {
						if(oldestIndex == -1 || checked.timeCheckedMS < oldestTime) {
							oldestIndex = i;
							oldestTime = checked.timeCheckedMS;
						}
					}
				}
				
				if(!removedOne) {
					// just replace the oldest one with us, no need to change array
					RequirementsCheckedUser checked = recentlyPassedCheckUsers.get(oldestIndex);
					checked.timeCheckedMS = System.currentTimeMillis();
					checked.username = username;
				}else {
					recentlyPassedCheckUsers.add(new RequirementsCheckedUser(username, System.currentTimeMillis()));
				}
			}else {
				recentlyPassedCheckUsers.add(new RequirementsCheckedUser(username, System.currentTimeMillis()));
			}
		}
		return result;
	}

	
	@Override
	protected void onFailedInteractCheck(Thing thing) {
		super.onFailedInteractCheck(thing);
		
		if(thing instanceof Comment) {
			Comment comment = (Comment) thing;
			
			logger.info(String.format("Reporting comment id=%s by %s because it failed an interaction check", comment.id(), comment.author()));
			super.handleReport(comment.fullname(), "loansbot autoreport");
		}
	}

	@Override
	protected void handleBanUserOnAllSubreddits(String userToBan, String banMessage, String banReason, String banNote) {
		MappingDatabase db = (MappingDatabase) database;
		Username userToBanUsername = db.getUsernameMapping().fetchByUsername(userToBan);
		if(userToBanUsername == null) {
			logger.info(String.format("Tried to ban %s but we don't know any users by that name.", userToBan));
			return;
		}
		
		super.handleBanUserOnAllSubreddits(userToBan, banMessage, banReason, banNote);
	}
	
	
	@Override
	protected void onSuccessfullyBannedUser(String username) {
		super.onSuccessfullyBannedUser(username);

		Timestamp now = new Timestamp(System.currentTimeMillis());
		MappingDatabase db = (MappingDatabase) database;
		Username usernameObj = db.getUsernameMapping().fetchByUsername(username);
		db.getBannedUserMapping().save(new BannedUser(-1, usernameObj.userId, now, now));
	}

	@Override
	protected void onSuccessfullyUnbanUser(String username) {
		super.onSuccessfullyUnbanUser(username);

		MappingDatabase db = (MappingDatabase) database;
		Username usernameObj = db.getUsernameMapping().fetchByUsername(username);
		db.getBannedUserMapping().removeByUserID(usernameObj.userId);
	}

	@Override
	protected void handleUnbanUserOnAllSubreddits(final String userToUnban) {
		MappingDatabase db = (MappingDatabase) database;
		Username userToUnbanUsername = db.getUsernameMapping().fetchByUsername(userToUnban);
		if(userToUnbanUsername == null) {
			logger.info(String.format("Tried to unban %s but we don't know any users by that name.", userToUnban));
			return;
		}
		
		/*
		if(!db.getBannedUserMapping().containsUserID(userToUnbanUsername.userId)) {
			logger.info(String.format("Tried to unban %s but we didn't ban them.", userToUnban));
			return;
		}
		*/
		
		super.handleUnbanUserOnAllSubreddits(userToUnban);
	}
	
	
}
