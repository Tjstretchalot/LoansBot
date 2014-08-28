package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		super(SummonType.LOAN, LOAN_PATTERN);
		
		logger = LogManager.getLogger();
	}

	@Override
	public void parse(String doer, String doneTo, String url, String group) throws ParseException {
		this.doer = doer;
		this.doneTo = doneTo;
		this.url = url;
		
		if(doer == null || doneTo == null || url == null)
			throw new IllegalArgumentException("Regular loan summons require the doer, doneTo, and url");
		
		String[] split = group.split("\\s");
		String number = split[1].replace("$", "");
		amountPennies = getPennies(number);

		if(amountPennies <= 0)
			throw new ParseException("Negative amount of money", split[0].length() + 1);
	}

	@Override
	public String applyChanges(FileConfiguration config, Database database)
			throws SQLException {
		Loan loan = new Loan(amountPennies, doer, doneTo, 0, false, System.currentTimeMillis(), 0);
		loan.setOriginalThread(url);
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
