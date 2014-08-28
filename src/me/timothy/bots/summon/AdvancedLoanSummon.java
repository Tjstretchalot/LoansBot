package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;

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
		super(SummonType.ADV_LOAN, LOAN_PATTERN_ADVANCED);
		
		logger = LogManager.getLogger();
	}

	@Override
	public void parse(String doer, String doneTo, String url, String text) throws ParseException {
		this.doer = doer;
		
		if(doer == null)
			throw new IllegalArgumentException("Advanced loan summons require a do-er");
		
		String[] split = text.split("\\s");
		this.doneTo = getUser(split[1]);
		String number = split[2].replace("$", "");
		amountPennies = getPennies(number);

		if(amountPennies <= 0)
			throw new ParseException("Negative amount of money", split[0].length() + split[1].length() + 2);
	}

	@Override
	public String applyChanges(FileConfiguration config, Database database)
			throws SQLException {
		Loan loan = new Loan(amountPennies, doer, doneTo, 0, false, System.currentTimeMillis(), 0);
		logger.printf(Level.INFO, "%s just lent %s to %s", doer, LoansBotUtils.getCostString(amountPennies / 100.), doneTo);
		
		database.addLoan(loan);
		return config.getSuccessfulLoan()
				.replace("<lender>", loan.getLender())
				.replace("<borrower>", loan.getBorrower())
				.replace("<amount>", LoansBotUtils.getCostString(loan.getAmountPennies()/100.));
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
