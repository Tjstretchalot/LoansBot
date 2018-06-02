package me.timothy.bots.summon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.summon.patterns.PatternFactory;
import me.timothy.bots.summon.patterns.SummonMatcher;
import me.timothy.bots.summon.patterns.SummonPattern;
import me.timothy.jreddit.info.Comment;

/**
 * For when someone does not pay back ones loan
 * in an adequate amount of time
 * 
 * @author Timothy
 */
public class UnpaidSummon implements CommentSummon {
	/**
	 * Matches things like
	 * 
	 * $unpaid /u/John
	 * $unpaid /u/Asdf_Jkl
	 */
	private static final SummonPattern UNPAID_PATTERN = new PatternFactory()
			.addLiteral("$unpaid")
			.addUsername("user1")
			.build();
	

	private Logger logger;

	public UnpaidSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public boolean mightInteractWith(Comment comment, Database db, FileConfiguration config) {
		return UNPAID_PATTERN.matcher(comment.body()).find();
	}
	
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(comment.author().equalsIgnoreCase(config.getProperty("user.username")))
			return null;
		
		SummonMatcher matcher = UNPAID_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			ResponseInfo responseInfo = matcher.group();
			ResponseInfoFactory.addCommentDetails(responseInfo, comment);
			
			LoansDatabase database = (LoansDatabase) db;

			String author = responseInfo.getObject("author").toString();
			String user1 = responseInfo.getObject("user1").toString();
			Username authorUsername = database.getUsernameMapping().fetchByUsername(author);
			Username user1Username = database.getUsernameMapping().fetchByUsername(user1);
			
			List<Loan> relevantLoans = new ArrayList<Loan>();
			if(authorUsername != null && user1Username != null) {
				relevantLoans = database.getLoanMapping().fetchWithBorrowerAndOrLender(user1Username.userId, authorUsername.userId, true);
			}
			List<Loan> changed = new ArrayList<>();
			
			for(Loan l : relevantLoans) {
				if(l.principalRepaymentCents != l.principalCents) {					
					l.unpaid = true;
					database.getLoanMapping().save(l);
					changed.add(l);
				}
			}
			responseInfo.addTemporaryString("changed loans", LoansBotUtils.getLoansAsTable(changed, database, changed.size()));
			
			logger.printf(Level.INFO, "%s has defaulted on %d loans from %s", user1Username == null ? "null user " + user1 : user1Username.username, changed.size(), authorUsername == null ? ("null user '" + author + "'") : authorUsername.username);
			
			String responseFormat = database.getResponseMapping().fetchByName("unpaid").responseBody;
			
			// Also send pms to all affected lenders telling them about the default
			List<PMResponse> lenderPMs = new ArrayList<>();
			if(user1Username != null) {
				List<Loan> loansWithBorrowerThatDefaulted = database.getLoanMapping().fetchWithBorrowerAndOrLender(user1Username.userId, 0, false);
				Set<Integer> userIdsWithBorrowerThatDefaulted = new HashSet<>();
				for(Loan loanWithBorrowerThatDefaulted : loansWithBorrowerThatDefaulted)
				{
					int lenderId = loanWithBorrowerThatDefaulted.lenderId;

					if(lenderId != authorUsername.userId)
					{
						userIdsWithBorrowerThatDefaulted.add(lenderId);
					}
				}

				String affectedPMTitleFormat = database.getResponseMapping().fetchByName("unpaid_affected_lender_alert_pm_title").responseBody;
				String affectedPMFormat = database.getResponseMapping().fetchByName("unpaid_affected_lender_alert_pm_text").responseBody;
				for(int userIdWithBorrowerThatDefaulted : userIdsWithBorrowerThatDefaulted)
				{
					List<Username> usernames = database.getUsernameMapping().fetchByUserId(userIdWithBorrowerThatDefaulted);

					for(Username username : usernames)
					{
						ResponseInfo pmTitleResponseInfo = new ResponseInfo(ResponseInfoFactory.base);
						pmTitleResponseInfo.addTemporaryString("lender", authorUsername != null ? authorUsername.username : author);
						pmTitleResponseInfo.addTemporaryString("borrower", user1Username.username);
						pmTitleResponseInfo.addTemporaryString("affected", username.username);
						
						ResponseInfo pmTextResponseInfo = new ResponseInfo(ResponseInfoFactory.base);
						pmTextResponseInfo.addTemporaryString("lender", authorUsername != null ? authorUsername.username : author);
						pmTextResponseInfo.addTemporaryString("borrower", user1Username.username);
						pmTextResponseInfo.addTemporaryString("affected", username.username);
						pmTextResponseInfo.addTemporaryString("comment permalink", comment.linkURL());
						
						String titleText = new ResponseFormatter(affectedPMTitleFormat, pmTitleResponseInfo).getFormattedResponse(config, database);
						String messageText = new ResponseFormatter(affectedPMFormat, pmTextResponseInfo).getFormattedResponse(config, database);

						lenderPMs.add(new PMResponse(username.username, titleText, messageText));
					}
				}
			}
			
			// add the reminder pm
			String reminderPMTitleFormat = database.getResponseMapping().fetchByName("unpaid_lender_reminder_pm_title").responseBody;
			String reminderPMFormat = database.getResponseMapping().fetchByName("unpaid_lender_reminder_pm_text").responseBody;
			
			ResponseInfo reminderPMTitleResponseInfo = new ResponseInfo(ResponseInfoFactory.base);
			reminderPMTitleResponseInfo.addTemporaryString("lender", authorUsername != null ? authorUsername.username : author);
			reminderPMTitleResponseInfo.addTemporaryString("borrower", user1Username != null ? user1Username.username : user1);
			
			ResponseInfo reminderPMResponseInfo = new ResponseInfo(ResponseInfoFactory.base);
			reminderPMResponseInfo.addTemporaryString("lender", authorUsername != null ? authorUsername.username : author);
			reminderPMResponseInfo.addTemporaryString("borrower", user1Username != null ? user1Username.username : user1);
			reminderPMResponseInfo.addTemporaryString("comment permalink", comment.linkURL());
			
			String reminderTitle = new ResponseFormatter(reminderPMTitleFormat, reminderPMTitleResponseInfo).getFormattedResponse(config, database);
			String reminderText = new ResponseFormatter(reminderPMFormat, reminderPMResponseInfo).getFormattedResponse(config, database);
			
			lenderPMs.add(new PMResponse(authorUsername != null ? authorUsername.username : author, reminderTitle, reminderText));
			
			// ban if at least 1 changed loan
			boolean banUser = changed.size() > 0;
			String banMessage = null, banReason = null, banNote = null, userToBan = null;

			if(banUser) {
				ResponseInfo banMessageAndReasonInfo = new ResponseInfo(ResponseInfoFactory.base);
				banMessageAndReasonInfo.addTemporaryString("lender", authorUsername.username);
				banMessageAndReasonInfo.addTemporaryString("borrower", user1Username.username);
				banMessageAndReasonInfo.addTemporaryString("comment permalink", comment.linkURL());

				banMessage = new ResponseFormatter(database.getResponseMapping().fetchByName("unpaid_ban_message").responseBody, banMessageAndReasonInfo).getFormattedResponse(config, database);
				banReason = new ResponseFormatter(database.getResponseMapping().fetchByName("unpaid_ban_reason").responseBody, banMessageAndReasonInfo).getFormattedResponse(config, database);
				banNote = new ResponseFormatter(database.getResponseMapping().fetchByName("unpaid_ban_note").responseBody, banMessageAndReasonInfo).getFormattedResponse(config, database);
				userToBan = user1Username.username;
			}
			
			//return new SummonResponse(SummonResponse.ResponseType.VALID, new ResponseFormatter(responseFormat, responseInfo).getFormattedResponse(config, database), null, lenderPMs);
			return new SummonResponse(SummonResponse.ResponseType.VALID, new ResponseFormatter(responseFormat, responseInfo).getFormattedResponse(config, database), null, lenderPMs, null,
					banUser, userToBan, banMessage, banReason, banNote, false, null);
		}
		return null;
	}
}
