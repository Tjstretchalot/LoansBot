package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.jreddit.info.Message;

/**
 * When the bot is summoned to make a loan using the username
 * as well as the fullname of the comment. This is for when the
 * user the loan is being made to cannot be guessed, such as in
 * Pm's
 * 
 * @author Timothy
 *
 */
public class AdvancedLoanSummon extends Summon {
	/**
	 * Matches things like:
	 * 
	 * $loan /u/Bob 50 t3_asdf12
	 */
	private static final Pattern LOAN_PATTERN_ADVANCED = Pattern.compile("\\s*\\$loan\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?\\st3_\\S{4,10}");
	private Logger logger;

	private String doer;
	private String doneTo;

	private int amountPennies;

	public AdvancedLoanSummon() {
		logger = LogManager.getLogger();
	}
	
	

	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.message.Message)
	 */
	@Override
	public boolean parse(Message message) throws UnsupportedOperationException {
		Matcher matcher = LOAN_PATTERN_ADVANCED.matcher(message.body());
		if(matcher.find()) {
			String text = matcher.group().trim();
			
			this.doer = message.author();
			String[] split = text.split("\\s");
			this.doneTo = BotUtils.getUser(split[1]);
			String number = split[2].replace("$", "");
			try {
				amountPennies = BotUtils.getPennies(number);
			} catch (ParseException e) {
				 // This is easily recovered from but shouldn't happen since the regex won't match invalid numbers
				logger.warn(e);
				return false;
			}

			if(amountPennies <= 0)
				return false;
			
			return true;
		}
		
		return false;
		
	}



	@Override
	public String applyChanges(FileConfiguration con, Database database) {
		LoansFileConfiguration config = (LoansFileConfiguration) con;
		try {
			Loan loan = new Loan(amountPennies, doer, doneTo, 0, false, System.currentTimeMillis(), 0);
			logger.printf(Level.INFO, "%s just lent %s to %s", doer, BotUtils.getCostString(amountPennies / 100.), doneTo);

			((LoansDatabase) database).addLoan(loan);
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
