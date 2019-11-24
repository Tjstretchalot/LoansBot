package me.timothy.bots.summon.loan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;

public class BorrowerGaveLoanTrigger implements LoanTrigger {
	/**
	 * The response name for the title.
	 * 
	 * Substitutions:
	 * <ul>
	 * 	<li>lender - the primary username of the lender for the new loan, who 
	 *  is an active borrower</li>
	 *  <li>lender_with_tag - the usernames of the lender with a tag</li>
	 *  <li>borrower - the primary username of the borrower</li>
	 *  <li>borrower_with_tag - the usernames of the borrower with a tag</li>
	 *  <li>loan_thread - the thread where the loan command is</li>
	 *  <li>money - the formatted amount of the loan</li>
	 * </ul>
	 */
	public static final String TITLE_RESPONSE_NAME = "active_borrower_created_loan_modmail_pm_title";

	/**
	 * The response name for the title.
	 * 
	 * Substitutions:
	 * <ul>
	 * 	<li>lender - the primary username of the lender for the new loan, who 
	 *  is an active borrower</li>
	 *  <li>lender_with_tag - the usernames of the lender with a tag</li>
	 *  <li>borrower - the primary username of the borrower</li>
	 *  <li>borrower_with_tag - the usernames of the borrower with a tag</li>
	 *  <li>loan_thread - the thread where the loan command is</li>
	 *  <li>money - the formatted amount of the loan</li>
	 * </ul>
	 */
	public static final String BODY_RESPONSE_NAME = "active_borrower_created_loan_modmail_pm_body";
	
	private Logger logger;
	
	public BorrowerGaveLoanTrigger() {
		logger = LogManager.getLogger();
	}
	
	@Override
	public boolean essential() {
		return false;
	}

	@Override
	public void onNewLoan(LoanSummonContext ctx) {
		int numOutstandingAsBorrower = ctx.database.getLoanMapping().fetchNumberOfOutstandingLoansWithUserAsBorrower(ctx.lender.id);
		if (numOutstandingAsBorrower <= 0)
			return;

		
		String lenderName = LoansBotUtils.formatUsernamesSeparatedWith(ctx.lenderUsernames, " aka. ", true);
		String borrowerName = LoansBotUtils.formatUsernamesSeparatedWith(ctx.borrowerUsernames, " aka. ", true);
		MoneyFormattableObject money = new MoneyFormattableObject(ctx.amountPennies);
		
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		respInfo.addTemporaryString("lender", ctx.lenderUsernames.get(0).username);
		respInfo.addTemporaryString("lender_with_tag", lenderName);
		respInfo.addTemporaryString("borrower", ctx.borrowerUsernames.get(0).username);
		respInfo.addTemporaryString("borrower_with_tag", borrowerName);
		respInfo.addTemporaryString("loan_thread", ctx.requestThreadURL);
		respInfo.addTemporaryObject("money", money);

		logger.info("Detected borrower switching roles to lender: %s. He made a loan to %s (principal: %s)",
				lenderName, borrowerName, money.toFormattedString(respInfo, "money", ctx.config, ctx.database));
		
		ctx.addPMResponse(
				LoansBotUtils.createPMFromFormat(
						"/r/" + LoansBotUtils.PRIMARY_SUBREDDIT, 
						TITLE_RESPONSE_NAME, BODY_RESPONSE_NAME, 
						ctx.database, ctx.config, respInfo)
		);
	}
}
