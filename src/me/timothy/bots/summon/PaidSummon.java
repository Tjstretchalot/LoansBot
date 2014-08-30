package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Message;

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
		logger = LogManager.getLogger();
	}
	
	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.comment.Comment)
	 */
	@Override
	public boolean parse(Comment comment) throws UnsupportedOperationException {
		return parse(comment.author(), comment.body());
	}

	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.message.Message)
	 */
	@Override
	public boolean parse(Message message) throws UnsupportedOperationException {
		return parse(message.author(), message.body());
	}

	private boolean parse(String author, String text) {
		Matcher matcher = PAID_PATTERN.matcher(text);
		
		if(matcher.find()) {
			String group = matcher.group().trim();
			String[] split = group.split("\\s");
			
			this.doer = author;
			this.doneTo = BotUtils.getUser(split[1]);
			String number = split[2].replace("$", "");

			try {
				amountPennies = BotUtils.getPennies(number);
			} catch (ParseException e) {
				logger.warn(e);
				return false;
			}

			if(amountPennies <= 0)
				return false;
			
			return true;
		}
		return false;
	}

	public String applyChanges(FileConfiguration con, Database db) {
		LoansFileConfiguration config = (LoansFileConfiguration) con;
		LoansDatabase database = (LoansDatabase) db;
		try {
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

			long time = System.currentTimeMillis();
			for(Loan l : relevantLoans) {
				if(l.getAmountPaidPennies() < l.getAmountPennies()) {
					changedLoans.add(l);
					if(l.getAmountPennies() - l.getAmountPaidPennies() <= remainingPennies) {
						int amount = (l.getAmountPennies() - l.getAmountPaidPennies());
						if(amount > 0) {
							remainingPennies -= amount;
							database.payLoan(l.getId(), amount);
							database.setLoanPaidBackInFullDate(l.getId(), time);
							l.setDatePaidBackFullJUTC(time);
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
			response = response.replace("<amount paid>", BotUtils.getCostString(amountPennies / 100.));
			response = response.replace("<overpayment>", BotUtils.getCostString(remainingPennies / 100.));
			response = response.replace("<loans>", LoansBotUtils.getLoansString(changedLoans, doer, config));

			logger.info(doer + " has been repaid " + amountPennies + " by " + doneTo);
			return response;
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
