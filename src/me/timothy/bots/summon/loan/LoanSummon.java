package me.timothy.bots.summon.loan;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.currencies.CurrencyHandler;
import me.timothy.bots.models.CreationInfo;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.summon.CommentSummon;
import me.timothy.bots.summon.PMResponse;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.bots.summon.patterns.PatternFactory;
import me.timothy.bots.summon.patterns.SummonMatcher;
import me.timothy.bots.summon.patterns.SummonPattern;
import me.timothy.jreddit.info.Comment;

/**
 * For creating a loan where the user the loan is being made out to can be
 * easily and consistently guessed, such as in comments. 
 * 
 * The actual core logic for these loans is fairly straightforward, but there
 * is a lot of business logic associated with new loans. For example, if a 
 * borrower makes a loan then the moderators should be messaged. If a new
 * lender makes a loan a different message should be sent to the moderators,
 * with its own specific variables, etc.
 * 
 * To split this up we create a "LoanSummonContext" which contains the local
 * variables in the handleComment function. If you prefer to think of this
 * as an event system, the loan summon context corresponds to the "loan 
 * created" event arguments. Then we pass this along to a specified list of
 * handlers (aka listeners if you prefer).
 * 
 * @author Timothy
 */
public class LoanSummon implements CommentSummon {
	/**
	 * Matches things like:
	 * 
	 * $loan 50 $loan 50.00 $loan $50.00 $loan 50.00$ $loan 50.00 EUR $loan 50 EUR
	 * $loan 50 USD
	 */
	private static final SummonPattern LOAN_PATTERN = new PatternFactory().addCaseInsensLiteral("$loan")
			.addMoney("money1").addCurrency("convert_from", true).build();
	
	private static final LoanTrigger[] TRIGGERS = new LoanTrigger[] {
			new NewLenderTrigger(),
			new LenderReceivedLoanTrigger(),
			new BorrowerGaveLoanTrigger()
	};

	private Logger logger;

	public LoanSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public boolean mightInteractWith(Comment comment, Database db, FileConfiguration config) {
		return LOAN_PATTERN.matcher(comment.body()).find();
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(comment.author().equalsIgnoreCase(config.getProperty("user.username"))) {
			return null;
		}
		
		SummonMatcher matcher = LOAN_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			LoansDatabase database = (LoansDatabase) db;
			ResponseInfo respInfo = matcher.group();
			ResponseInfoFactory.addCommentDetails(respInfo, comment);
			
			if(respInfo.getObject("author").toString().equals(respInfo.getObject("link_author").toString()))
				return null;

			String author = respInfo.getObject("author").toString();
			String linkAuthor = respInfo.getObject("link_author").toString();
			String url = respInfo.getObject("link_url").toString();
			MoneyFormattableObject moneyObj = (MoneyFormattableObject) respInfo.getObject("money1");
			int amountPennies = moneyObj.getAmount();
			
			String convertFrom = respInfo.getObject("convert_from") != null ? respInfo.getObject("convert_from").toString() : null;
			boolean hasConversion = convertFrom != null;
			if(hasConversion) {
				CurrencyHandler inst = CurrencyHandler.getInstance();
				double conversionRate = inst.getConversionRate(convertFrom, "USD");
				if(inst.exceededSubscriptionPlanUntil != null) {
					logger.warn("Unable to service currency conversion for " + author + " - exceeded subscription plan");
					respInfo.addTemporaryString("exceeded_until", inst.exceededSubscriptionPlanUntil.getTime().toString());
					String exPlanResp = database.getResponseMapping().fetchByName("currency_conv_exceeded_plan").responseBody;
					return new SummonResponse(SummonResponse.ResponseType.INVALID,
							new ResponseFormatter(exPlanResp, respInfo).getFormattedResponse(config, database));
				}
				
				logger.debug("Converting from " + convertFrom + " to USD using rate " + conversionRate);
				respInfo.addTemporaryString("convert_from", convertFrom);
				respInfo.addTemporaryString("conversion_rate", Double.toString(conversionRate));
				amountPennies *= conversionRate;
				
				moneyObj.setAmount(amountPennies);
			}
			
			User doerU = database.getUserMapping().fetchOrCreateByName(author);
			User doneToU = database.getUserMapping().fetchOrCreateByName(linkAuthor);
			long now = Math.round(comment.createdUTC() * 1000);
			
			List<PMResponse> pmResponses = new ArrayList<>();
			HashMap<String, List<Object>> specialResponses = new HashMap<>();
			
			LoanSummonContext ctx = new LoanSummonContext(
					database, config, doerU, doneToU, 
					database.getUsernameMapping().fetchByUserId(doerU.id), 
					database.getUsernameMapping().fetchByUserId(doneToU.id), 
					url, amountPennies, pmResponses, specialResponses);
			for(LoanTrigger trigger : TRIGGERS) {
				try {
					trigger.onNewLoan(ctx);
				}catch(Exception e) {
					if(trigger.essential()) {
						throw e;
					}else {
						logger.catching(e);
						ctx.addPMResponse(LoansBotUtils.exceptionPM(trigger.getClass().getCanonicalName(), e));
					}
				}
			}
			
			Loan loan = new Loan(-1, doerU.id, doneToU.id, amountPennies, 0, false, false, null, new Timestamp(now), new Timestamp(now), null);
			CreationInfo cInfoRetro = attemptRetroactiveLoan(database, loan); // this may set the loan id, which will cause it to be updated rather than added
			database.getLoanMapping().save(loan);
			respInfo.addTemporaryString("loan id", Integer.toString(loan.id));
			
			CreationInfo cInfo = null;
			if(cInfoRetro != null) {
				cInfo = new CreationInfo(cInfoRetro.id, loan.id, CreationInfo.CreationType.REDDIT, 
						url, null, -1, new Timestamp(Math.min(now, cInfoRetro.createdAt.getTime())),
						new Timestamp(Math.max(now, cInfoRetro.createdAt.getTime())));
			}else {
				cInfo = new CreationInfo(-1, loan.id, CreationInfo.CreationType.REDDIT, url, null, -1, new Timestamp(now), new Timestamp(now));
			}
			database.getCreationInfoMapping().save(cInfo);
			
			logger.printf(Level.INFO, "%s just lent %s to %s [loan %d] [retroactive = %s]", author, BotUtils.getCostString(amountPennies / 100.), linkAuthor, loan.id, cInfoRetro == null ? "no" : "yes");
			
			String resp = null;
			
			if(hasConversion) {
				resp  = database.getResponseMapping().fetchByName("successful_loan_with_conversion").responseBody;
			}else {
				resp = database.getResponseMapping().fetchByName("successful_loan").responseBody;
			}
			return new SummonResponse(
					SummonResponse.ResponseType.VALID, 
					new ResponseFormatter(resp, respInfo).getFormattedResponse(config, database), 
					"991c8042-3ecc-11e4-8052-12313d05258a",
					pmResponses,
					null,
					false,
					null,
					null,
					null,
					null,
					false,
					null, 
					specialResponses);
		}
		return null;
	}

	private CreationInfo attemptRetroactiveLoan(LoansDatabase database, Loan loan) {
		// We want to find creation infos for a loan that might match this.

		List<Loan> similarLoans = database.getLoanMapping().fetchWithBorrowerAndOrLender(loan.borrowerId, loan.lenderId,
				true);
		int[] similarLoanIds = new int[similarLoans.size()];
		for (int i = 0; i < similarLoans.size(); i++) {
			similarLoanIds[i] = similarLoans.get(i).id;
		}

		List<CreationInfo> similarCreationInfos = database.getCreationInfoMapping().fetchManyByLoanIds(similarLoanIds);

		for (Loan simLoan : similarLoans) {
			CreationInfo simLoanInfo = null;
			for (CreationInfo cInfo : similarCreationInfos) {
				if (cInfo.loanId == simLoan.id) {
					simLoanInfo = cInfo;
					break;
				}
			}
			if (simLoanInfo == null)
				continue;

			if (simLoanInfo.type == CreationInfo.CreationType.PAID_SUMMON
					&& simLoan.principalCents >= loan.principalCents) {
				// This is the loan!
				loan.id = simLoan.id;
				return simLoanInfo;
			}
		}
		return null;
	}

}
