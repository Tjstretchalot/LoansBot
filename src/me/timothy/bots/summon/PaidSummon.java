package me.timothy.bots.summon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.currencies.CurrencyHandler;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.GenericFormattableObject;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.jreddit.info.Comment;

import org.apache.logging.log4j.Level;
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
	 * $paid /u/asdf 50.00 EUR
	 * $paid /u/fdaa 5 USD
	 */
	private static final Pattern PAID_PATTERN_WITH_CURRENCY = Pattern.compile("\\s*(\\$paid\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?)(\\s[A-Z]{3})?");
	private static final String PAID_FORMAT = "$paid <user1> <money1>";

	private Logger logger;
	
	public PaidSummon() {
		logger = LogManager.getLogger();
	}

	/**
	 * Goes through each relevant loan and attempts to add as
	 * much as possible to the principal repayment without 
	 * setting the repayment to more than the principal or
	 * using more than the remaining pennies (in total)
	 * <br><br>
	 * When this function completes, <i>relevantLoans</i> only contains
	 * the number of CHANGED LOANS
	 * 
	 * @param relevantLoans relevant loans
	 * @param remainingPennies remaining pennies
	 * @param database database
	 * @return money remaining and changed loans (indirectly)
	 */
	private int updateLoans(List<Loan> relevantLoans, int remainingPennies, LoansDatabase database) {
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
		
		relevantLoans.clear();
		relevantLoans.addAll(changedLoans);
		return remainingPennies;
	}

	/**
	 * Removes all loans where pricipalCents == principalRepaymentCents
	 * 
	 * @param loans
	 */
	private void removeFinishedLoans(List<Loan> loans) {
		for(int i = 0; i < loans.size(); i++) {
			if(loans.get(i).principalCents == loans.get(i).principalRepaymentCents) {
				loans.remove(i);
				i--;
			}
		}
	}
	
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		LoansDatabase database = (LoansDatabase) db;
		Matcher matcher = PAID_PATTERN_WITH_CURRENCY.matcher(comment.body());
		
		if(matcher.find()) {
			ResponseInfo respInfo = ResponseInfoFactory.getResponseInfo(PAID_FORMAT, matcher.group(1).trim(), comment);
			
			String author = respInfo.getObject("author").toString();
			String user1 = respInfo.getObject("user1").toString();
			MoneyFormattableObject moneyObj = (MoneyFormattableObject) respInfo.getObject("money1");
			int amountRepaid = moneyObj.getAmount();
			boolean hasConversion = matcher.group(2) != null; 
			if(hasConversion) {
				String convertFrom = matcher.group(2).trim();
				double conversionRate = CurrencyHandler.getConversionRate(convertFrom, "USD");
				logger.debug("Converting from " + convertFrom + " to USD using rate " + conversionRate);
				respInfo.addTemporaryString("convert_from", convertFrom);
				respInfo.addTemporaryString("conversion_rate", Double.toString(conversionRate));
				amountRepaid *= conversionRate;
				
				moneyObj.setAmount(amountRepaid);
			}
			Username authorUsername = database.getUsernameByUsername(author);
			Username user1Username = database.getUsernameByUsername(user1);
			
			if(authorUsername == null || user1Username == null) {
				logger.printf(Level.WARN, "%s tried to say %s repaid him by %d, but author is %s and user 1 is %s",
						author, user1, amountRepaid, (authorUsername == null ? "null" : "not null"), (user1Username == null ? "null" : "not null"));
				ResponseFormatter formatter = new ResponseFormatter(database.getResponseByName("no_loans_to_repay").responseBody, respInfo);
				return new SummonResponse(SummonResponse.ResponseType.INVALID, formatter.getFormattedResponse(config, database));//.replace("<borrower>", doneTo).replace("<author>", doer));
			}
			
			User authorUser = database.getUserById(authorUsername.userId);
			User user1User = database.getUserById(user1Username.userId);
			
			if(amountRepaid <= 0) {
				logger.printf(Level.WARN, "Ridiculous amount repaid of %d, ignoring", amountRepaid);
				return null;
			}
			
			List<Loan> relevantLoans = database.getLoansWithBorrowerAndOrLender(user1User.id, authorUser.id, true);
			removeFinishedLoans(relevantLoans);
			
			if(relevantLoans.size() == 0) {
				logger.printf(Level.WARN, "%s tried to say %s repaid him by %d, but there are no ongoing loans", author, user1, amountRepaid);
//				ResponseFormatter formatter = new ResponseFormatter(database.getResponseByName("no_loans_to_repay").responseBody, respInfo);
//				return new SummonResponse(SummonResponse.ResponseType.INVALID, formatter.getFormattedResponse(config, database));
//				Loan loan = new Loan(-1, )
			}
			
			
			
			int interest = updateLoans(relevantLoans, amountRepaid, database);
			respInfo.addTemporaryObject("interest", new MoneyFormattableObject(interest));
			respInfo.addTemporaryObject("changed loans", new GenericFormattableObject(LoansBotUtils.getLoansString(relevantLoans, database, author, config)));
			logger.printf(Level.INFO, "%s has repaid %s by $%s with %s interest over %d loans", user1, author,
					BotUtils.getCostString(amountRepaid), respInfo.getObject("interest").toFormattedString(respInfo, "interest", config, database), relevantLoans.size());
			String response = null;
			
			if(hasConversion) {
				response = database.getResponseByName("repayment_with_conversion").responseBody;
			}else {
				response = database.getResponseByName("repayment").responseBody;
			}
			
			
			ResponseFormatter formatter = new ResponseFormatter(response, respInfo);
			return new SummonResponse(SummonResponse.ResponseType.VALID, formatter.getFormattedResponse(config, database));
		}
		return null;
	}

}
