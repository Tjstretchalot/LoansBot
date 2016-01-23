package me.timothy.bots.summon;

import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.currencies.CurrencyHandler;
import me.timothy.bots.models.CreationInfo;
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
	 * $loan 50.00 EUR
	 * $loan 50 EUR
	 * $loan 50 USD
	 */
	private static final Pattern LOAN_PATTERN = Pattern.compile("(\\s*\\$loan\\s\\$?\\d+\\.?\\d*\\$?)(\\s[A-Z]{3})?");
	private static final String LOAN_FORMAT = "$loan <money1>";
	
	private Logger logger;
	
	public LoanSummon() {
		logger = LogManager.getLogger();
	}
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = LOAN_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String withoutCurrency = matcher.group(1);
			LoansDatabase database = (LoansDatabase) db;
			ResponseInfo respInfo = ResponseInfoFactory.getResponseInfo(LOAN_FORMAT, withoutCurrency.trim(), comment);
			
			if(respInfo.getObject("author").toString().equals(respInfo.getObject("link_author").toString()))
				return null;

			String author = respInfo.getObject("author").toString();
			String linkAuthor = respInfo.getObject("link_author").toString();
			String url = respInfo.getObject("link_url").toString();
			MoneyFormattableObject moneyObj = (MoneyFormattableObject) respInfo.getObject("money1");
			int amountPennies = moneyObj.getAmount();
			boolean hasConversion = matcher.group(2) != null; 
			if(hasConversion) {
				String convertFrom = matcher.group(2).trim();
				double conversionRate = CurrencyHandler.getConversionRate(convertFrom, "USD");
				logger.debug("Converting from " + convertFrom + " to USD using rate " + conversionRate);
				respInfo.addTemporaryString("convert_from", convertFrom);
				respInfo.addTemporaryString("conversion_rate", Double.toString(conversionRate));
				amountPennies *= conversionRate;
				
				moneyObj.setAmount(amountPennies);
			}
			
			User doerU = database.getOrCreateUserByUsername(author);
			User doneToU = database.getOrCreateUserByUsername(linkAuthor);
			long now = Math.round(comment.createdUTC() * 1000);
			
			
			
			Loan loan = new Loan(-1, doerU.id, doneToU.id, amountPennies, 0, false, false, null, new Timestamp(now), new Timestamp(now), null);
			CreationInfo cInfoRetro = attemptRetroactiveLoan(database, loan); // this may set the loan id, which will cause it to be updated rather than added
			database.addOrUpdateLoan(loan);
			CreationInfo cInfo = null;
			if(cInfoRetro != null) {
				cInfo = new CreationInfo(cInfoRetro.id, loan.id, CreationInfo.CreationType.REDDIT, 
						url, null, -1, new Timestamp(Math.min(now, cInfoRetro.createdAt.getTime())),
						new Timestamp(Math.max(now, cInfoRetro.createdAt.getTime())));
			}else {
				cInfo = new CreationInfo(-1, loan.id, CreationInfo.CreationType.REDDIT, url, null, -1, new Timestamp(now), new Timestamp(now));
			}
			database.addOrUpdateCreationInfo(cInfo);
			
			logger.printf(Level.INFO, "%s just lent %s to %s [loan %d] [retroactive = %s]", author, BotUtils.getCostString(amountPennies / 100.), linkAuthor, loan.id, cInfoRetro == null ? "no" : "yes");
			
			String resp = null;
			
			if(hasConversion) {
				resp  = database.getResponseByName("successful_loan_with_conversion").responseBody;
			}else {
				resp = database.getResponseByName("successful_loan").responseBody;
			}
			return new SummonResponse(SummonResponse.ResponseType.VALID, new ResponseFormatter(resp, respInfo).getFormattedResponse(config, database));
		}
		return null;
	}
	
	private CreationInfo attemptRetroactiveLoan(LoansDatabase database, Loan loan) {
		// We want to find creation infos for a loan that might match this.
		
		List<Loan> similarLoans = database.getLoansWithBorrowerAndOrLender(loan.borrowerId, loan.lenderId, true);
		int[] similarLoanIds = new int[similarLoans.size()];
		for(int i = 0; i < similarLoans.size(); i++) {
			similarLoanIds[i] = similarLoans.get(i).id;
		}
		
		List<CreationInfo> similarCreationInfos = database.getCreationInfoByLoanId(similarLoanIds);
		
		for(Loan simLoan : similarLoans) {
			CreationInfo simLoanInfo = null;
			for(CreationInfo cInfo : similarCreationInfos) {
				if(cInfo.loanId == simLoan.id) {
					simLoanInfo = cInfo;
					break;
				}
			}
			if(simLoanInfo == null)
				continue;
			
			if(simLoanInfo.type == CreationInfo.CreationType.PAID_SUMMON && simLoan.principalCents >= loan.principalCents) {
				// This is the loan!
				loan.id = simLoanInfo.id;
				return simLoanInfo;
			}
		}
		return null;
	}

}
