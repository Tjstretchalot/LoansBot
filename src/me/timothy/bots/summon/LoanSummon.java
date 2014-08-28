package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jreddit.comment.Comment;

public class LoanSummon extends Summon {
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
	private String url;
	private int amountPennies;
	
	public LoanSummon() {
		logger = LogManager.getLogger();
	}
	
	
	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.comment.Comment)
	 */
	@Override
	public boolean parse(Comment comment) throws UnsupportedOperationException {
		Matcher matcher = LOAN_PATTERN.matcher(comment.getComment());
		
		if(matcher.find()) {
			this.doer = comment.getAuthor();
			this.doneTo = comment.getLinkAuthor();
			this.url = comment.getLinkURL();
			
			String text = matcher.group().trim();
			String[] split = text.split("\\s");
			String number = split[1].replace("$", "");
			
			try {
				this.amountPennies = BotUtils.getPennies(number);
			} catch (ParseException e) {
				logger.warn(e);
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String applyChanges(FileConfiguration config, Database db) {
		LoansDatabase database = (LoansDatabase) db;
		try {
			Loan loan = new Loan(amountPennies, doer, doneTo, 0, false, System.currentTimeMillis(), 0);
			loan.setOriginalThread(url);
			logger.printf(Level.INFO, "%s just lent %s to %s", doer, BotUtils.getCostString(amountPennies / 100.), doneTo);

			database.addLoan(loan);
			return config.getSuccessfulLoan()
					.replace("<lender>", loan.getLender())
					.replace("<borrower>", loan.getBorrower())
					.replace("<amount>", BotUtils.getCostString(loan.getAmountPennies()/100.));
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * @return the person who performed the summon
	 */
	public String getDoer() {
		return doer;
	}

	/**
	 * @return the person the summon was done to
	 */
	public String getDoneTo() {
		return doneTo;
	}

	/**
	 * @return the amount, in pennies, of the loan
	 */
	public int getAmountPennies() {
		return amountPennies;
	}

}
