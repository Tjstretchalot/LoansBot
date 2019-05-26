package me.timothy.bots.summon;

import java.sql.Timestamp;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.functions.IInviteToLendersCampFunction;
import me.timothy.bots.models.DelayedVettingRequest;
import me.timothy.bots.models.PromotionBlacklist;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.summon.SummonResponse.ResponseType;
import me.timothy.bots.summon.patterns.PatternFactory;
import me.timothy.bots.summon.patterns.SummonMatcher;
import me.timothy.bots.summon.patterns.SummonPattern;
import me.timothy.jreddit.info.Message;

/**
 * This is invoked after a user starts lending enough that they meet a threshold that the
 * moderators vet them to ensure they don't have any obvious red flags.
 * 
 * @author Timothy
 */
public class VettedSummon implements PMSummon {
	private static final SummonPattern SUBJECT_PATTERN = new PatternFactory()
			.addLiteral(null, "re:", true, false)
			.addLiteral("Vetting")
			.addLiteral("Required:")
			.addUsername("user")
			.build();
	
	private static final SummonPattern IS_VETTED_PATTERN = new PatternFactory()
			.addCaseInsensLiteral("$vetted")
			.addCaseInsensLiteral("success")
			.build();
	
	private static final SummonPattern IS_NOT_VETTED_PATTERN = new PatternFactory()
			.addCaseInsensLiteral("$vetted")
			.addCaseInsensLiteral("failure")
			.addQuotedString("reason")
			.build();
	
	private static final SummonPattern REVISIT_PATTERN = new PatternFactory()
			.addCaseInsensLiteral("$vetted")
			.addCaseInsensLiteral("revisit")
			.addQuotedString("reason")
			.addInteger("number_loans")
			.build();
	/**
	 * The thing that invites people to lenders camp
	 */
	public IInviteToLendersCampFunction inviter;
	
	/**
	 * Create a new vetted summon which uses the given function to invite people
	 * to lenders camp after they are vetted
	 * 
	 * @param inviter the invitation function
	 */
	public VettedSummon(IInviteToLendersCampFunction inviter) {
		this.inviter = inviter;
	}
	
	/**
	 * Create a new vetted summon with inviter not set. It must be set later; this this a workaround
	 * for the order that summons are typically initialized, where we don't know enough information to
	 * create this function yet
	 */
	public VettedSummon() {
		
	}
	
	@Override
	public SummonResponse handlePM(Message message, Database db, FileConfiguration config) {
		LoansDatabase ldb = (LoansDatabase)db;
		
		SummonMatcher subjMatcher = SUBJECT_PATTERN.matcher(message.subject());
		if(!subjMatcher.find())
			return null;
		
		// Ahh! This is probably worth refactoring if it gets any crazier
		boolean vetted = false;
		boolean revisit = false;
		String reason = null;
		int numberLoans = -1;
		if(IS_VETTED_PATTERN.matcher(message.body()).find()) {
			vetted = true;
		}else {
			SummonMatcher matcher = IS_NOT_VETTED_PATTERN.matcher(message.body());
			if(!matcher.find()) {
				matcher = REVISIT_PATTERN.matcher(message.body());
				if(!matcher.find()) 
					return null;

				ResponseInfo respInfo = matcher.group();
				reason = respInfo.getObject("reason").toString();
				numberLoans = Integer.valueOf(respInfo.getObject("number_loans").toString());
				revisit = true;
			}else {
				ResponseInfo respInfo = matcher.group();
				reason = respInfo.getObject("reason").toString();
			}
		}
		
		if(!isAuthorized(message, ldb, config)) 
			return notAuthorized(message, ldb, config);
		
		ResponseInfo info = subjMatcher.group();
		String usernameStr = info.getObject("user").toString();
		
		if(usernameStr == null)
			return unknownUser(message, ldb, config);
		
		Username username = ldb.getUsernameMapping().fetchByUsername(usernameStr);
		if(username == null)
			return unknownUser(message, ldb, config);
		
		boolean onPromoBlacklist = ldb.getPromotionBlacklistMapping().contains(username.userId);
		if(!onPromoBlacklist)
			return invalidUser(message, ldb, config, username.username);
		
		if(vetted) {
			inviter.inviteToLendersCamp(username);
			ldb.getPromotionBlacklistMapping().remove(username.userId);
			return validAndVetted(message, ldb, config, username.username);
		}else if(revisit) {
			DelayedVettingRequest req = ldb.getDelayedVettingRequestMapping().fetchByUserId(username.userId);
			if(req == null) {
				req = new DelayedVettingRequest(-1, username.userId, 
						numberLoans, reason, new Timestamp(System.currentTimeMillis()), null);
			}else {
				req.numberLoans = numberLoans;
				req.reason = reason;
			}
			
			ldb.getDelayedVettingRequestMapping().save(req);
			return validAndDelayed(message, ldb, config, username.username, reason, numberLoans);
		}else {
			ldb.getPromotionBlacklistMapping().remove(username.userId);
			ldb.getPromotionBlacklistMapping().save(new PromotionBlacklist(-1, username.userId, 
					ldb.getUsernameMapping().fetchByUsername(config.getProperty("user.username")).userId,
					reason, new Timestamp(System.currentTimeMillis()), null));
			return validAndNotVetted(message, ldb, config, username.username, reason);
		}
	}
	
	private boolean isAuthorized(Message message, LoansDatabase ldb, FileConfiguration config) {
		if("borrow".equals(message.subreddit())) {
			return true;
		}
		
		Username username = ldb.getUsernameMapping().fetchByUsername(message.author());
		if(username == null)
			return false;
		
		User user = ldb.getUserMapping().fetchById(username.userId);
		return user.auth >= 5;
	}
	
	/**
	 * Returns the summon response used when the user that sent us the message is not authorized to vet people
	 * 
	 * @param message the original message
	 * @param ldb the database
	 * @param config the file configuration
	 * @return the response to the user
	 */
	private SummonResponse notAuthorized(Message message, LoansDatabase ldb, FileConfiguration config) {
		String bodyFormat = ldb.getResponseMapping().fetchByName("vetted_not_authorized_body").responseBody;
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		
		if(message.subreddit() != null)
			respInfo.addTemporaryString("author", message.subreddit());
		else
			respInfo.addTemporaryString("author", message.author());
		
		String body = new ResponseFormatter(bodyFormat, respInfo).getFormattedResponse(config, ldb);
		
		return new SummonResponse(ResponseType.INVALID, body);
	}
	
	/**
	 * Returns the summon response used when we have absolutely no idea who they might be talking about. 
	 * We figure out the author from the title, which is "Vetting Required: /u/(user)".
	 * 
	 * @param message the original message
	 * @param ldb the loans database
	 * @param config the file configuration
	 * @return the summon response to use
	 */
	private SummonResponse unknownUser(Message message, LoansDatabase ldb, FileConfiguration config) {
		String bodyFormat = ldb.getResponseMapping().fetchByName("vetted_unknown_user_body").responseBody;
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		
		if(message.subreddit() != null)
			respInfo.addTemporaryString("author", message.subreddit());
		else
			respInfo.addTemporaryString("author", message.author());
		
		respInfo.addTemporaryString("subject", message.subject());
		
		String body = new ResponseFormatter(bodyFormat, respInfo).getFormattedResponse(config, ldb);
		
		return new SummonResponse(ResponseType.INVALID, body);
	}
	
	/**
	 * Returns the summon response used when we think we know who they are talking about, but it doesn't make
	 * sense to apply a vetted command on them. In particular, the user is not on the promotion blacklist.
	 * 
	 * @param message the original message
	 * @param ldb the database
	 * @param config the file configuration
	 * @return the summon response to use
	 */
	private SummonResponse invalidUser(Message message, LoansDatabase ldb, FileConfiguration config, String user) {
		String bodyFormat = ldb.getResponseMapping().fetchByName("vetted_invalid_user_body").responseBody;
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);

		if(message.subreddit() != null)
			respInfo.addTemporaryString("author", message.subreddit());
		else
			respInfo.addTemporaryString("author", message.author());
		
		respInfo.addTemporaryString("user", user);
		
		String body = new ResponseFormatter(bodyFormat, respInfo).getFormattedResponse(config, ldb);
		
		return new SummonResponse(ResponseType.INVALID, body);
	}
	
	/**
	 * The response when the pm was valid and the user was removed from the promotion blacklist
	 * 
	 * @param message the original message
	 * @param ldb the loans database
	 * @param config the file configuration
	 * @param user the user who was vetted
	 * @return the response to make
	 */
	private SummonResponse validAndVetted(Message message, LoansDatabase ldb, FileConfiguration config, String user) { 
		String bodyFormat = ldb.getResponseMapping().fetchByName("vetted_user_vetted_success_body").responseBody;
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);

		if(message.subreddit() != null)
			respInfo.addTemporaryString("author", message.subreddit());
		else
			respInfo.addTemporaryString("author", message.author());
		
		respInfo.addTemporaryString("user", user);
		
		String body = new ResponseFormatter(bodyFormat, respInfo).getFormattedResponse(config, ldb);
		
		return new SummonResponse(ResponseType.INVALID, body);
	}
	
	/**
	 * The response when the pm was valid and the user was not removed from the promotion blacklist
	 * 
	 * @param message the original message
	 * @param ldb the loans database
	 * @param config the file configuration
	 * @param user the user who failed the vetting process
	 * @param reason the reason they failed the vetting process
	 * @return the response to make
	 */
	private SummonResponse validAndNotVetted(Message message, LoansDatabase ldb, FileConfiguration config, String user, String reason) {
		String bodyFormat = ldb.getResponseMapping().fetchByName("vetted_user_not_vetted_success_body").responseBody;
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);

		if(message.subreddit() != null)
			respInfo.addTemporaryString("author", message.subreddit());
		else
			respInfo.addTemporaryString("author", message.author());
		
		respInfo.addTemporaryString("user", user);
		respInfo.addTemporaryString("reason", reason);
		
		String body = new ResponseFormatter(bodyFormat, respInfo).getFormattedResponse(config, ldb);
		
		return new SummonResponse(ResponseType.INVALID, body);
	}
	
	private SummonResponse validAndDelayed(Message message, LoansDatabase ldb, FileConfiguration config, String user, String reason, int numberLoans) {
		String bodyFormat = ldb.getResponseMapping().fetchByName("vetted_user_delayed_success_body").responseBody;
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);

		if(message.subreddit() != null)
			respInfo.addTemporaryString("author", message.subreddit());
		else
			respInfo.addTemporaryString("author", message.author());
		
		respInfo.addTemporaryString("user", user);
		respInfo.addTemporaryString("reason", reason);
		respInfo.addTemporaryString("number_loans", Integer.toString(numberLoans));
		
		String body = new ResponseFormatter(bodyFormat, respInfo).getFormattedResponse(config, ldb);
		
		return new SummonResponse(ResponseType.INVALID, body);
	}
}
