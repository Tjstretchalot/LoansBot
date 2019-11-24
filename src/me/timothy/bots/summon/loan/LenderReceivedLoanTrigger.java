package me.timothy.bots.summon.loan;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.models.PromotionBlacklist;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.specresps.RemoveFromLendersCamp;

/**
 * When a lender receives a loan and they aren't on the promotion blacklist:
 * <ul>
 * <li>Message the moderators of the primary subreddit</li>
 * <li>Add them to the blacklist</li>
 * <li>Ensure they are removed from lenderscamp</li>
 * </ul>
 * 
 * @author Timothy
 */
public class LenderReceivedLoanTrigger implements LoanTrigger {
	/**
	 * The response name for the title.
	 * 
	 * Substitutions:
	 * <ul>
	 * 	<li>lender - the username of the lender for the new loan</li>
	 *  <li>author - alternate name for lender</li>
	 *  <li>lender_with_tag - all the usernames for the lender with reddit tags</li> 
	 *  <li>borrower - the username of the borrower for the new loan, who was a lender</li>
	 *  <li>borrower_with_tag - all the usernames for the borrower with reddit tags</li>
	 *  <li>loan_thread - the thread where the loan command is</li>
	 *  <li>money - the formatted amount of the loan</li>
	 *  <li>money1 - alternate name for money</li>
	 * </ul>
	 */
	public static final String TITLE_RESPONSE_NAME = "lender_received_loan_modmail_pm_title";

	/**
	 * The response name for the title.
	 * 
	 * Substitutions:
	 * <ul>
	 * 	<li>lender - the username of the lender for the new loan</li>
	 *  <li>author - alternate name for lender</li>
	 *  <li>lender_with_tag - all the usernames for the lender with reddit tags</li> 
	 *  <li>borrower - the username of the borrower for the new loan, who was a lender</li>
	 *  <li>borrower_with_tag - all the usernames for the borrower with reddit tags</li>
	 *  <li>loan_thread - the thread where the loan command is</li>
	 *  <li>money - the formatted amount of the loan</li>
	 *  <li>money1 - alternate name for money</li>
	 * </ul>
	 */
	public static final String BODY_RESPONSE_NAME = "lender_received_loan_modmail_pm_body";
	
	private Logger logger;
	
	public LenderReceivedLoanTrigger() {
		logger = LogManager.getLogger();
	}

	
	@Override
	public boolean essential() {
		return true;
	}
	
	@Override
	public void onNewLoan(LoanSummonContext ctx) {
		if (ctx.borrower.auth >= 1)
			return;
		
		int numBorrowerStartedAsLender = ctx.database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(ctx.borrower.id);
		if(numBorrowerStartedAsLender <= 0) 
			return;
		
		PromotionBlacklist blacklist = ctx.database.getPromotionBlacklistMapping().fetchByUserId(ctx.borrower.id);
		if(blacklist != null) 
			return;
		
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		
		String lenderName = LoansBotUtils.formatUsernamesSeparatedWith(ctx.lenderUsernames, " aka. ", true);
		String borrowerName = LoansBotUtils.formatUsernamesSeparatedWith(ctx.borrowerUsernames, " aka. ", true);
		MoneyFormattableObject money = new MoneyFormattableObject(ctx.amountPennies);

		respInfo.addTemporaryString("lender", ctx.lenderUsernames.get(0).username);
		respInfo.addTemporaryString("lender_with_tag", lenderName);
		respInfo.addTemporaryString("author", ctx.lenderUsernames.get(0).username);
		respInfo.addTemporaryString("borrower", ctx.borrowerUsernames.get(0).username);
		respInfo.addTemporaryString("borrower_with_tag", borrowerName);
		respInfo.addTemporaryString("loan_thread", ctx.requestThreadURL);
		respInfo.addTemporaryObject("money", money);
		respInfo.addTemporaryObject("money1", money);

		logger.info("Detected lender switching roles to borrower: %s. He received a loan from %s (principal: %s)",
				borrowerName, lenderName, money.toFormattedString(respInfo, "money", ctx.config, ctx.database));
		
		// Send PM
		try {
			ctx.addPMResponse(
					LoansBotUtils.createPMFromFormat(
							"/r/" + LoansBotUtils.PRIMARY_SUBREDDIT, 
							TITLE_RESPONSE_NAME, BODY_RESPONSE_NAME, 
							ctx.database, ctx.config, respInfo)
			);
		}catch(Exception e) {
			logger.catching(e);
			ctx.addPMResponse(LoansBotUtils.exceptionPM(getClass().getCanonicalName(), e));
		}
		
		// Add to blacklist
		User loansBot = ctx.database.getUserMapping().fetchOrCreateByName(ctx.config.getProperty("user.username"));
		ctx.database.getPromotionBlacklistMapping().save(
				new PromotionBlacklist(
						-1, ctx.borrower.id, loansBot.id, "Received loan", 
						new Timestamp(System.currentTimeMillis()),
						new Timestamp(System.currentTimeMillis()))
				);
		
		// Remove from lenderscamp.
		// This isn't technically necessary since it
		// will be done automatically in a delayed manner after the new
		// promo blacklist entry is detected. However, by doing this we force
		// ourselves to sync with reddit on these users which is defensive
		for(Username borrowerUname : ctx.borrowerUsernames) {
			ctx.addSpecialResponse(
					RemoveFromLendersCamp.SPECIAL_KEY, 
					new RemoveFromLendersCamp(borrowerUname.username));
		}
	}
}
