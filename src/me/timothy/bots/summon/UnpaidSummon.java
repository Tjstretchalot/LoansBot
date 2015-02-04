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
import me.timothy.bots.models.User;
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
			ResponseInfo responseInfo = ResponseInfoFactory.getResponseInfo(UNPAID_FORMAT, group);
			LoansDatabase database = (LoansDatabase) db;

			User author = database.getUserByUsername(responseInfo.getObject("author").toString());
			User user1U = database.getUserByUsername(responseInfo.getObject("user1").toString());
			
			List<Loan> relevantLoans = (author != null && user1U != null) ? database.getLoansWithBorrowerAndOrLender(user1U.id, author.id, true) : new ArrayList<Loan>();
			List<Loan> changed = new ArrayList<>();
			
			for(Loan l : relevantLoans) {
				if(l.principalRepaymentCents != l.principalCents) {
					database.setLoanUnpaid(l, true);
					changed.add(l);
				}
			}
			responseInfo.addTemporaryString("changed loans", LoansBotUtils.getLoansAsTable(changed, database, changed.size()));
			logger.printf(Level.INFO, "%s has defaulted on %d loans from %s", user1U.username, changed.size(), author.username);
			
			String responseFormat = config.getString("unpaid");
			return new SummonResponse(SummonResponse.ResponseType.VALID, new ResponseFormatter(responseFormat, responseInfo).getFormattedResponse(config, database));
		}
		return null;
	}

}
