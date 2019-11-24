package me.timothy.bots.summon.loan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;

/**
 * Send a message to the primary subreddit when a user makes a loan for the 
 * first time.
 * 
 * @author Timothy
 */
public class NewLenderTrigger implements LoanTrigger {
	/**
	 * The response name for the title.
	 * 
	 * Substitutions:
	 * <ul>
	 * 	<li>lender - the primary username of the lender</li>
	 *  <li>author - alternate name for lender</li>
	 *  <li>lender_with_tag - all the usernames for the lender with reddit tags</li> 
	 *  <li>borrower - the username of the borrower</li>
	 *  <li>borrower_with_tag - all the usernames for the borrower with reddit tags</li>
	 *  <li>loan_thread - the thread where the loan command is</li>
	 *  <li>money - the formatted amount of the loan</li>
	 *  <li>money1 - alternate name for money</li>
	 * </ul>
	 */
	public static final String TITLE_RESPONSE_NAME = "new_lender_modmail_pm_title";
	
	/**
	 * The response name for the body.
	 * 
	 * Substitutions:
	 * <ul>
	 * 	<li>lender - the primary username of the lender</li>
	 *  <li>author - alternate name for lender</li>
	 *  <li>lender_with_tag - all the usernames for the lender with reddit tags</li> 
	 *  <li>borrower - the username of the borrower</li>
	 *  <li>borrower_with_tag - all the usernames for the borrower with reddit tags</li>
	 *  <li>loan_thread - the thread where the loan command is</li>
	 *  <li>money - the formatted amount of the loan</li>
	 *  <li>money1 - alternate name for money</li>
	 * </ul>
	 */
	public static final String BODY_RESPONSE_NAME = "new_lender_modmail_pm_body";

	private Logger logger;
	
	public NewLenderTrigger() {
		logger = LogManager.getLogger();
	}
	
	@Override
	public boolean essential() {
		return false;
	}
	
	@Override
	public void onNewLoan(LoanSummonContext ctx) {
		int numLenderStartedAsLender = ctx.database.getLoanMapping()
				.fetchNumberOfLoansCompletedWithUserAsLender(ctx.lender.id)[0];
		if(numLenderStartedAsLender >= 1)
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

		logger.info("Detected new lender: %s made his first loan. The borrower was %s (principal: %s)",
				lenderName, borrowerName, money.toFormattedString(respInfo, "money", ctx.config, ctx.database));
		
		ctx.addPMResponse(
				LoansBotUtils.createPMFromFormat(
						"/r/" + LoansBotUtils.PRIMARY_SUBREDDIT, 
						TITLE_RESPONSE_NAME, BODY_RESPONSE_NAME, 
						ctx.database, ctx.config, respInfo)
		);
	}
}
