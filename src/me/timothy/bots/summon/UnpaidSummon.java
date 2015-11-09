package me.timothy.bots.summon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = UNPAID_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String group = matcher.group().trim();
			ResponseInfo responseInfo = ResponseInfoFactory.getResponseInfo(UNPAID_FORMAT, group, comment);
			LoansDatabase database = (LoansDatabase) db;

			String author = responseInfo.getObject("author").toString();
			String user1 = responseInfo.getObject("user1").toString();
			Username authorUsername = database.getUsernameByUsername(author);
			Username user1Username = database.getUsernameByUsername(user1);
			
			List<Loan> relevantLoans = new ArrayList<Loan>();
			if(authorUsername != null && user1Username != null) {
				relevantLoans = database.getLoansWithBorrowerAndOrLender(user1Username.userId, authorUsername.userId, true);
			}
			List<Loan> changed = new ArrayList<>();
			
			for(Loan l : relevantLoans) {
				if(l.principalRepaymentCents != l.principalCents) {
					database.setLoanUnpaid(l, true);
					changed.add(l);
				}
			}
			responseInfo.addTemporaryString("changed loans", LoansBotUtils.getLoansAsTable(changed, database, changed.size()));
			
			logger.printf(Level.INFO, "%s has defaulted on %d loans from %s", user1Username.username, changed.size(), authorUsername == null ? ("null user '" + author + "'") : authorUsername.username);
			
			String responseFormat = database.getResponseByName("unpaid").responseBody;
			return new SummonResponse(SummonResponse.ResponseType.VALID, new ResponseFormatter(responseFormat, responseInfo).getFormattedResponse(config, database));
		}
		return null;
	}

}
