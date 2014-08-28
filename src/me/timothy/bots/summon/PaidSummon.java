package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;

public class PaidSummon extends Summon {
	/**
	 * Matches things like
	 * 
	 * $paid /u/John 50.00
	 * $paid /u/Asdf $50.00
	 * $paid /u/Jk_jl 50.00$
	 */
	private static final Pattern PAID_PATTERN = Pattern.compile("\\s*\\$paid\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?");
	
	private Logger logger;
	
	private String doer;
	private String doneTo;
	
	private int amountPennies;
	
	public PaidSummon() {
		super(SummonType.PAID, PAID_PATTERN);
		
		logger = LogManager.getLogger();
	}

	@Override
	public void parse(String doer, String doneTo, String url, String text) throws ParseException {
		this.doer = doer;
		
		if(doer == null)
			throw new IllegalArgumentException("Paid summons require a do-er");
		
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
		if(amountPennies <= 0) {
			logger.warn("User attempted to pay back a negative amount of money, ignoring");
			return null;
		}
		
		List<Loan> relevantLoans = database.getLoansWith(doneTo, doer);
		
		if(relevantLoans.isEmpty()) {
			logger.warn("???, Someone tried to pay someone who never lent money to him!");
			return config.getNoLoansToRepay().replace("<borrower>", doneTo).replace("<author>", doer);
		}
		
		
		
		int remainingPennies = amountPennies;
		List<Loan> changedLoans = new ArrayList<>();
		
		for(Loan l : relevantLoans) {
			if(l.getAmountPaidPennies() < l.getAmountPennies()) {
				changedLoans.add(l);
				if(l.getAmountPennies() - l.getAmountPaidPennies() <= remainingPennies) {
					int amount = (l.getAmountPennies() - l.getAmountPaidPennies());
					if(amount > 0) {
						remainingPennies -= amount;
						database.payLoan(l.getId(), amount);
						database.setLoanPaidBackInFullDate(l.getId(), System.currentTimeMillis());
						l.setAmountPaidPennies(l.getAmountPennies());

						if(l.isUnpaid()) {
							l.setUnpaid(false);
							database.setLoanUnpaid(l.getId(), false);
						}
						
						if(remainingPennies == 0)
							break;
					}
				}else {
					database.payLoan(l.getId(), remainingPennies);
					l.setAmountPaidPennies(l.getAmountPaidPennies() + remainingPennies);
					remainingPennies = 0;
					break;
				}
			}
		}
		
		String response = config.getRepayment();
		response = response.replace("<lender>", doer);
		response = response.replace("<borrower>", doneTo);
		response = response.replace("<amount paid>", LoansBotUtils.getCostString(amountPennies / 100.));
		response = response.replace("<overpayment>", LoansBotUtils.getCostString(remainingPennies / 100.));
		response = response.replace("<loans>", LoansBotUtils.getLoansString(changedLoans, doer, config));
		
		logger.info(doer + " has been repaid " + amountPennies + " by " + doneTo);
		return response;
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
