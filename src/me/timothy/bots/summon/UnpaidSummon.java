package me.timothy.bots.summon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final Pattern UNPAID_PATTERN = Pattern.compile("\\s*\\$unpaid\\s/u/\\S+");
	private static final String UNPAID_FORMAT = "$unpaid <user1>";

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
		
		Matcher matcher = UNPAID_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String group = matcher.group().trim();
			ResponseInfo responseInfo = ResponseInfoFactory.getResponseInfo(UNPAID_FORMAT, group, comment);
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


				String pmFormat = database.getResponseMapping().fetchByName("unpaid_lender_pm").responseBody;
				for(int userIdWithBorrowerThatDefaulted : userIdsWithBorrowerThatDefaulted)
				{
					List<Username> usernames = database.getUsernameMapping().fetchByUserId(userIdWithBorrowerThatDefaulted);

					for(Username username : usernames)
					{
						ResponseInfo pmResponseInfo = new ResponseInfo(ResponseInfoFactory.base);
						pmResponseInfo.addTemporaryString("lender", username.username);
						pmResponseInfo.addTemporaryString("borrower", user1Username.username);
						pmResponseInfo.addTemporaryString("comment permalink", comment.linkURL());

						String messageText = new ResponseFormatter(pmFormat, pmResponseInfo).getFormattedResponse(config, database);

						lenderPMs.add(new PMResponse(username.username, "Unpaid Borrower Notification", messageText));
					}
				}
			}

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
