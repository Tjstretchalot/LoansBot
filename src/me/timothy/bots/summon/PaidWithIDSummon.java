package me.timothy.bots.summon;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Repayment;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.GenericFormattableObject;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.summon.patterns.PatternFactory;
import me.timothy.bots.summon.patterns.SummonMatcher;
import me.timothy.bots.summon.patterns.SummonPattern;
import me.timothy.jreddit.info.Comment;

/**
 * Allows lenders to mark the specific loan that they want their repayment to
 * go towards. Matches messages of the form $paid_with_id 15 50 USD
 * 
 * @author Timothy
 */
public class PaidWithIDSummon implements CommentSummon {

	/***
	 * Matches things like
	 * 	$paid_with_id 11057 100
	 *  $paid_with_id 87123 $25 USD
	 *  $paid_with_id 4512 75 GBP 	
	 */
	private static final SummonPattern PAID_PATTERN = new PatternFactory()
			.addLiteral("$paid_with_id")
			.addInteger("loan_id")
			.addMoney("money1")
			.addCurrency("convert_from", true)
			.build();
	
	private static final Logger logger = LogManager.getLogger();

	@Override
	public boolean mightInteractWith(Comment comment, Database db, FileConfiguration config) {
		return PAID_PATTERN.matcher(comment.body()).find();
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(comment.author().equalsIgnoreCase(config.getProperty("user.username")))
			return null;
		LoansDatabase database = (LoansDatabase) db;
		SummonMatcher matcher = PAID_PATTERN.matcher(comment.body());
		if(matcher.find()) {
			ResponseInfo respInfo = matcher.group();
			ResponseInfoFactory.addCommentDetails(respInfo, comment);
			
			String author = respInfo.getObject("author").toString();
			MoneyFormattableObject moneyObj = (MoneyFormattableObject) respInfo.getObject("money1");
			int amountRepaid = moneyObj.getAmount();
			if (amountRepaid <= 0) {
				logger.printf(Level.WARN, "%s tried to use $paid_with_id for amount %d cents, ignoring as this is absurd",
						author, amountRepaid);
				return null;
			}
			int loanId = Integer.valueOf(respInfo.getObject("loan_id").toString());
			
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
				amountRepaid *= conversionRate;
				
				moneyObj.setAmount(amountRepaid);
			}
			
			Username authorUsername = database.getUsernameMapping().fetchByUsername(author);
			
			if (authorUsername == null) {
				logger.printf(Level.WARN, "%s tried to use $paid_with_id but the LoansBot has no information on him",
							  author);
				ResponseFormatter formatter = new ResponseFormatter(database.getResponseMapping().fetchByName("paid_with_id_invalid").responseBody, respInfo);
				return new SummonResponse(SummonResponse.ResponseType.INVALID, formatter.getFormattedResponse(config, database));
			}
			
			respInfo.addTemporaryString("author id", Integer.toString(authorUsername.userId));
			
			User authorUser = database.getUserMapping().fetchById(authorUsername.userId);
			
			Loan loan = database.getLoanMapping().fetchByID(loanId);
			if(loan == null || loan.deleted) {
				if (loan != null) {
					logger.printf(Level.WARN, "%s tried to repay loan %d which has been deleted", author, loanId);
				}
				ResponseFormatter formatter = new ResponseFormatter(database.getResponseMapping().fetchByName("paid_with_id_invalid").responseBody, respInfo);
				return new SummonResponse(SummonResponse.ResponseType.INVALID, formatter.getFormattedResponse(config, database));
			}
			
			if (loan.lenderId != authorUser.id && authorUser.auth < 5) {
				Username realLender = database.getUsernameMapping().fetchByUserId(loan.lenderId).get(0);
				
				logger.printf(Level.WARN, "%s tried to use $paid_with_id on loan %d which was by %s, but %s does not have sufficient authorization to do so", 
						author, loanId, realLender.username, author);
				ResponseFormatter formatter = new ResponseFormatter(database.getResponseMapping().fetchByName("paid_with_id_invalid").responseBody, respInfo);
				return new SummonResponse(SummonResponse.ResponseType.INVALID, formatter.getFormattedResponse(config, database));
			}
			
			if (loan.principalRepaymentCents == loan.principalCents) {
				logger.printf(Level.WARN, "%s tried to use $paid_with_id on loan %d which is already repaid.", 
						author, loanId);
				ResponseFormatter formatter = new ResponseFormatter(database.getResponseMapping().fetchByName("paid_with_id_invalid").responseBody, respInfo);
				return new SummonResponse(SummonResponse.ResponseType.INVALID, formatter.getFormattedResponse(config, database));
			}
			
			String borrowerUsername = database.getUsernameMapping().fetchByUserId(loan.borrowerId).get(0).username;
			respInfo.addTemporaryString("user1", borrowerUsername);
			respInfo.addTemporaryString("user1 id", Integer.toString(loan.borrowerId)); // for consistency in responses
			
			List<PMResponse> pmResponses = new ArrayList<>();
			String userToUnban = null;
			boolean unbanUser = false;
			
			int amountTowardPrincipal = Math.min(amountRepaid, loan.principalCents - loan.principalRepaymentCents);
			int interest = amountRepaid - amountTowardPrincipal;
			
			respInfo.addTemporaryObject("interest", new MoneyFormattableObject(interest));
			
			long time = System.currentTimeMillis();
			Repayment repayment = new Repayment(-1, loan.id, amountTowardPrincipal, new Timestamp(time), new Timestamp(time));
			database.getRepaymentMapping().save(repayment);
			loan.principalRepaymentCents += amountTowardPrincipal;
			loan.updatedAt = new Timestamp(time);
			if(loan.principalRepaymentCents == loan.principalCents && loan.unpaid) {
				loan.unpaid = false;
				database.getLoanMapping().save(loan);
				
				if (!PaidSummon.hasUnpaidLoans(database, loan.borrowerId)) {
					if (PaidSummon.UNBAN_ON_BORROW_WHEN_REPAID) {
						unbanUser = true;
						userToUnban = borrowerUsername;
					}
					
					pmResponses.add(PaidSummon.getPostRepayUnpaidModmail(database, config, respInfo));
				}
			}else {
				database.getLoanMapping().save(loan);
			}
			
			List<Loan> changedLoans = new ArrayList<>();
			changedLoans.add(database.getLoanMapping().fetchByID(loan.id)); // make sure this reflects new saved loan
			respInfo.addTemporaryObject("changed loans", new GenericFormattableObject(LoansBotUtils.getLoansString(changedLoans, database, author, config)));
			logger.printf(Level.INFO, "%s has repaid %s by $%s with %s interest over 1 loan", borrowerUsername, author,
					BotUtils.getCostString(amountTowardPrincipal / 100.0), respInfo.getObject("interest").toFormattedString(respInfo, "interest", config, database));
			
			String response = null;
			
			if(hasConversion) {
				response = database.getResponseMapping().fetchByName("repayment_with_conversion").responseBody;
			}else {
				response = database.getResponseMapping().fetchByName("repayment").responseBody;
			}
			
			ResponseFormatter formatter = new ResponseFormatter(response, respInfo);
			return new SummonResponse(SummonResponse.ResponseType.VALID, formatter.getFormattedResponse(config, database), null, pmResponses, null, false,
					null, null, null, null, unbanUser, userToUnban);
		}
		return null;
	}
	
}
