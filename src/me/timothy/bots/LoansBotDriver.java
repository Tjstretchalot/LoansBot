package me.timothy.bots;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import me.timothy.bots.models.Recheck;
import me.timothy.bots.models.ResetPasswordRequest;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.User;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.summon.CommentSummon;
import me.timothy.bots.summon.LinkSummon;
import me.timothy.bots.summon.PMSummon;
import me.timothy.jreddit.RedditUtils;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Errorable;
import me.timothy.jreddit.info.Link;
import me.timothy.jreddit.info.Listing;
import me.timothy.jreddit.info.Message;
import me.timothy.jreddit.info.Thing;

import org.json.simple.parser.ParseException;

/**
 * The bot driver for the loans bot
 * 
 * @author Timothy
 */
public class LoansBotDriver extends BotDriver {
	/**
	 * Exact echo of BotDriver constructor 
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

					String postfix = ((LoansDatabase) database).getResponseByName("secondary_subreddit_postfix").responseBody;
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
		sleepFor(2000);
		
		logger.debug("Scanning for recheck requests..");
		handleRechecks();
		sleepFor(2000);
		
		logger.debug("Scanning for reset password requests..");
		handleResetPasswordRequests();
		sleepFor(2000);
		
		super.doLoop();
	}
	
	/**
	 * Loops through unclaimed users who have a claim code but no claim
	 * link sent at and sends them a reddit PM with the claim code, then updates
	 * the claim link sent at to now.
	 */
	private void handleClaimCodes() {
		LoansDatabase ldb = (LoansDatabase) database;
		List<User> toSendCode = ldb.getUsersToSendCode();
		
		for(User user : toSendCode) {
			logger.info("Sending claim code to " + user.username);
			
			String message = ((LoansDatabase) database).getResponseByName("claim_code").responseBody;
			message = message.replace("<user>", user.username);
			message = message.replace("<code>", user.claimCode);
			message = message.replace("<codeurl>", "https://redditloans.com/users/" + user.id + "/claim/?code=" + user.claimCode);
			sendMessage(user.username, "RedditLoans Account Claimed", message);
			
			user.claimLinkSetAt = new Timestamp(System.currentTimeMillis());
			ldb.addOrUpdateUser(user);
			sleepFor(2000);
		}
	}
	
	/**
	 * Loops through all queued rechecks and handles
	 * them, then removes them from the queue
	 */
	private void handleRechecks() {
		LoansDatabase ldb = (LoansDatabase) database;
		
		List<Recheck> rechecks = ldb.getAllRechecks();
		if(rechecks.size() == 0)
			return;
		
		List<Recheck[]> batches = new ArrayList<Recheck[]>();
		
		while(rechecks.size() > 5) {
			Recheck[] batch = new Recheck[5];
			for(int i = 0; i < 5; i++) {
				batch[i] = rechecks.get(0);
				rechecks.remove(0);
			}
			batches.add(batch);
		}
		
		Recheck[] lastBatch = rechecks.toArray(new Recheck[0]);
		batches.add(lastBatch);
		
		logger.info("Performing " + batches.size() + " batches of rechecks");
		
		for(Recheck[] batch : batches) {
			String[] asStr = new String[batch.length];
			for(int i = 0; i < batch.length; i++) {
				asStr[i] = batch[i].fullname;
				ldb.deleteRecheck(batch[i]);
			}
			
			try {
				Listing listing = RedditUtils.getThings(asStr, bot.getUser());
				logger.debug(String.format("Batch size %d got %d things", batch.length, listing.numChildren()));
				for(int i = 0; i < listing.numChildren(); i++) {
					Thing thing = listing.getChild(i);
					
					handleRecheck(thing);
				}
			} catch (IOException | ParseException e) {
				logger.catching(e);
			}
		}
	}

	/**
	 * Handles a particular recheck by determining its type
	 * and calling the appropriate function in BotDriver
	 * @param thing the thing to recheck
	 */
	private void handleRecheck(Thing thing) {
		if(database.containsFullname(thing.fullname())) {
			logger.debug(String.format("Skipping %s because the database contains it", thing.fullname()));
			return;
		}
		
		if(thing instanceof Comment) {
			Comment comment = (Comment) thing;
			
			try {
				String linkId = comment.linkID();
				Listing listing = RedditUtils.getThings(new String[] { linkId }, bot.getUser());
				
				if(listing.numChildren() != 1) {
					logger.warn("Couldn't find link author for comment " + comment.fullname());
				}else {
					Link link = (Link) listing.getChild(0);
					comment.linkAuthor(link.author());
					comment.linkURL(link.url());
				}
			} catch (IOException | ParseException e) {
				logger.catching(e);
			}
				
			handleComment(comment, true);
		}else if(thing instanceof Link) {
			handleSubmission((Link) thing);
		}else if(thing instanceof Message) {
			handlePM(thing);
		}
	}
	/**
	 * Sends reset password links out to reddit users who requested
	 * a reset password link
	 */
	private void handleResetPasswordRequests() {
		LoansDatabase db = ((LoansDatabase) database);
		List<ResetPasswordRequest> resetPasswordRequests = db.getUnsentResetPasswordRequests();
		
		if(resetPasswordRequests.size() == 0) {
			return;
		}
		
		logger.debug(String.format("There are %d pending reset password requests", resetPasswordRequests.size()));
		
		for(ResetPasswordRequest rpr : resetPasswordRequests) {
			User user = db.getUserById(rpr.userId);
			if(user == null) {
				logger.warn(String.format("Reset Password Request id=%d has user_id=%d, which is not correlated with any user", rpr.id, rpr.userId));
				continue;
			}
			
			Response resp = db.getResponseByName("reset_password");
			ResponseInfo rInfo = new ResponseInfo();
			rInfo.addTemporaryString("userid", Integer.toString(user.id));
			rInfo.addTemporaryString("code", rpr.resetCode);
			logger.info(String.format("Sending reset password code to %s", user.username));
			sendMessage(user.username, "RedditLoans Reset Password", new ResponseFormatter(resp.responseBody, rInfo).getFormattedResponse(config, db));
			
			rpr.resetCodeSent = true;
			db.updateResetPasswordRequest(rpr);
		}
	}

	/**
	 * Sends a message to the specified user with the specified
	 * title & message
	 * @param user the user to send the message to
	 * @param title the title of the message
	 * @param message the text of the message
	 */
	private void sendMessage(final String to, final String title, final String message) {
		new Retryable<Boolean>("Send PM") {

			@Override
			protected Boolean runImpl() throws Exception {
				Errorable errors = bot.sendPM(to, title, message);
				List<?> errorsList = errors.getErrors();
				if(!errorsList.isEmpty()) {
					logger.warn("Failed to send " + message + " to " + to + ": " + errorsList.toString());
				}
				return true;
			}

		}.run();
	}
}
