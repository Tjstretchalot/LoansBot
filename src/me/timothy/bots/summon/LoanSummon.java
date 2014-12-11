package me.timothy.bots.summon;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
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
	
	private Logger logger;
	
	private String doer;
	private String doneTo;
	private int amountPennies;
	
	public LoanSummon() {
		logger = LogManager.getLogger();
	}
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = LOAN_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			this.doer = comment.author();
			this.doneTo = comment.linkAuthor();
			
			if(doer.toLowerCase().equals(doneTo.toLowerCase()))
				return null;
			String url = comment.linkURL();
			
			String text = matcher.group().trim();
			String[] split = text.split("\\s");
			String number = split[1].replace("$", "");
			
			try {
				this.amountPennies = BotUtils.getPennies(number);
			} catch (ParseException e) {
				logger.warn(e);
				return null;
			}
			
			LoansDatabase database = (LoansDatabase) db;
			User doerU = database.getOrCreateUserByUsername(doer);
			User doneToU = database.getOrCreateUserByUsername(doneTo);
			long now = System.currentTimeMillis();
			Loan loan = new Loan(-1, doerU.id, doneToU.id, amountPennies, 0, false, url, new Timestamp(now), new Timestamp(now));

			database.addOrUpdateLoan(loan);
			logger.printf(Level.INFO, "%s just lent %s to %s [loan %d]", doer, BotUtils.getCostString(amountPennies / 100.), doneTo, loan.id);
			String resp = config.getString("successful_loan")
					.replace("<lender>", doerU.username)
					.replace("<borrower>", doneToU.username)
					.replace("<amount>", BotUtils.getCostString(loan.principalCents/100.));
			return new SummonResponse(SummonResponse.ResponseType.VALID, resp);
		}
		return null;
	}

}
