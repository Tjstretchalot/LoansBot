package me.timothy.bots.summon;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.jreddit.info.Comment;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * For creating a loan where the user the loan is being made out
 * to can be easily and consistently guessed, such as in comments.
 * 
 * @author Timothy
 */
public class LoanSummon implements CommentSummon {
	/**
	 * Matches things like:
	 * 
	 * $loan 50
	 * $loan 50.00
	 * $loan $50.00
	 * $loan 50.00$
	 */
	private static final Pattern LOAN_PATTERN = Pattern.compile("\\s*\\$loan\\s\\$?\\d+\\.?\\d*\\$?");
	private static final String LOAN_FORMAT = "$loan <money1>";
	
	private Logger logger;
	
	public LoanSummon() {
		logger = LogManager.getLogger();
	}
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = LOAN_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			LoansDatabase database = (LoansDatabase) db;
			ResponseInfo respInfo = ResponseInfoFactory.getResponseInfo(LOAN_FORMAT, matcher.group().trim(), comment);
			
			if(respInfo.getObject("author").toString().equals(respInfo.getObject("link_author").toString()))
				return null;

			String author = respInfo.getObject("author").toString();
			String linkAuthor = respInfo.getObject("link_author").toString();
			String url = respInfo.getObject("link_url").toString();
			int amountPennies = ((MoneyFormattableObject) respInfo.getObject("money1")).getAmount();
			
			User doerU = database.getOrCreateUserByUsername(author);
			User doneToU = database.getOrCreateUserByUsername(linkAuthor);
			long now = System.currentTimeMillis();
			Loan loan = new Loan(-1, doerU.id, doneToU.id, amountPennies, 0, false, url, new Timestamp(now), new Timestamp(now));
			database.addOrUpdateLoan(loan);
			logger.printf(Level.INFO, "%s just lent %s to %s [loan %d]", author, BotUtils.getCostString(amountPennies / 100.), linkAuthor, loan.id);
			
			String resp = database.getResponseByName("successful_loan").responseBody;
			return new SummonResponse(SummonResponse.ResponseType.VALID, new ResponseFormatter(resp, respInfo).getFormattedResponse(config, database));
		}
		return null;
	}

}
