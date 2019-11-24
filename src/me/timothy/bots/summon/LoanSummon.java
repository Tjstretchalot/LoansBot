package me.timothy.bots.summon;

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
import me.timothy.bots.models.PromotionBlacklist;
import me.timothy.bots.models.User;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.specresps.RemoveFromLendersCamp;
import me.timothy.bots.summon.patterns.PatternFactory;
import me.timothy.bots.summon.patterns.SummonMatcher;
import me.timothy.bots.summon.patterns.SummonPattern;
import me.timothy.jreddit.info.Comment;

/**
 * For creating a loan where the user the loan is being made out to can be
 * easily and consistently guessed, such as in comments.
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
			HashMap<String, Object> specialResponses = new HashMap<>();
			int numLenderStartedAsLender = database.getLoanMapping().fetchNumberOfLoansCompletedWithUserAsLender(doerU.id)[0];
			if(numLenderStartedAsLender < 1) {
				String pmTitleFmt = database.getResponseMapping().fetchByName("new_lender_modmail_pm_title").responseBody;
				String pmBodyFmt = database.getResponseMapping().fetchByName("new_lender_modmail_pm_body").responseBody;
				
				String pmTitle = new ResponseFormatter(pmTitleFmt, respInfo).getFormattedResponse(config, database);
				String pmBody = new ResponseFormatter(pmBodyFmt, respInfo).getFormattedResponse(config, database);
				pmResponses.add(new PMResponse("/r/" + LoansBotUtils.PRIMARY_SUBREDDIT, pmTitle, pmBody));
			}

			int numBorrowerStartedAsLender = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(doneToU.id);
			if(numBorrowerStartedAsLender > 0 && doneToU.auth < 1) {
				PromotionBlacklist blacklist = database.getPromotionBlacklistMapping().fetchByUserId(doneToU.id);
				if(blacklist == null) {
					// Alert modmail
					ResponseInfo newLenderRespInfo = new ResponseInfo(ResponseInfoFactory.base);
					newLenderRespInfo.addLongtermString("borrower", linkAuthor);
					newLenderRespInfo.addLongtermString("lender", author);
					newLenderRespInfo.addLongtermString("loan thread", comment.linkURL());
					
					String pmTitleFmt = database.getResponseMapping().fetchByName("lender_received_loan_modmail_pm_title").responseBody;
					String pmBodyFmt = database.getResponseMapping().fetchByName("lender_received_loan_modmail_pm_body").responseBody;
					
					String pmTitle = new ResponseFormatter(pmTitleFmt, newLenderRespInfo).getFormattedResponse(config, database);
					String pmBody = new ResponseFormatter(pmBodyFmt, newLenderRespInfo).getFormattedResponse(config, database);
					pmResponses.add(new PMResponse("/r/" + LoansBotUtils.PRIMARY_SUBREDDIT, pmTitle, pmBody));
					
					// Add to blacklist
					User loansBot = database.getUserMapping().fetchOrCreateByName(config.getProperty("user.username"));
					database.getPromotionBlacklistMapping().save(
							new PromotionBlacklist(
									-1, doneToU.id, loansBot.id, "Received loan", 
									new Timestamp(System.currentTimeMillis()),
									new Timestamp(System.currentTimeMillis()))
							);
					
					// Remove from lenderscamp
					specialResponses.put(RemoveFromLendersCamp.SPECIAL_KEY, new RemoveFromLendersCamp(linkAuthor));
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
