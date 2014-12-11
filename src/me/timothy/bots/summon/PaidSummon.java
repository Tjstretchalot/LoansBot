package me.timothy.bots.summon;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.jreddit.info.Comment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Saying that a user has paid back in part or in full a loan
 * 
 * 
 * @author Timothy
 */
public class PaidSummon implements CommentSummon {
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

	public SummonResponse applyChanges(FileConfiguration config, Database db) {
		LoansDatabase database = (LoansDatabase) db;
		if(amountPennies <= 0) {
			logger.warn("User attempted to pay back a negative amount of money, ignoring");
			return null;
		}
		User doneToU = database.getUserByUsername(doneTo);
		User doerU = database.getUserByUsername(doer);
		List<Loan> relevantLoans = (doneToU != null && doerU != null) ? database.getLoansWithBorrowerAndOrLender(doneToU.id, doerU.id, true) : new ArrayList<Loan>();

		if(relevantLoans.isEmpty()) {
			logger.warn("???, Someone tried to pay someone who never lent money to him!");
			return new SummonResponse(SummonResponse.ResponseType.INVALID, config.getString("no_loans_to_repay").replace("<borrower>", doneTo).replace("<author>", doer));
		}



		int remainingPennies = amountPennies;
		List<Loan> changedLoans = new ArrayList<>();

		long time = System.currentTimeMillis();
		for(Loan l : relevantLoans) {
			if(l.principalRepaymentCents < l.principalCents) {
				changedLoans.add(l);
				if(l.principalCents - l.principalRepaymentCents <= remainingPennies) {
					int amount = (l.principalCents - l.principalRepaymentCents);
					if(amount > 0) {
						remainingPennies -= amount;
						database.payLoan(l, amount, time);

							if(l.unpaid) {
								database.setLoanUnpaid(l, false);
							}

						if(remainingPennies == 0)
							break;
					}
				}else {
					database.payLoan(l, remainingPennies, time);
					remainingPennies = 0;
					break;
				}
			}
		}
		
		String rem = BotUtils.getCostString(remainingPennies / 100.);
		String interest = 
				remainingPennies == 0 ? String.format("/u/%s has opted not to tell us the interest", doer) :
				String.format("In addition, /u/%s has paid $%s in interest", doneTo, rem);

		String response = config.getString("repayment");
		response = response.replace("<lender>", doer);
		response = response.replace("<borrower>", doneTo);
		response = response.replace("<amount paid>", BotUtils.getCostString(amountPennies / 100.));
		response = response.replace("<interest>", interest);
		response = response.replace("<loans>", LoansBotUtils.getLoansString(changedLoans, database, doer, config));

		logger.info(doer + " has been repaid " + amountPennies + " by " + doneTo);
		return new SummonResponse(SummonResponse.ResponseType.VALID, response);
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

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(parse(comment.author(), comment.body())) {
			return applyChanges(config, db);
		}
		return null;
	}

}
