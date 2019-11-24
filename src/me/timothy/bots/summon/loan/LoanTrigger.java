package me.timothy.bots.summon.loan;

/**
 * Describes something which can trigger on new loans. These are typically 
 * triggers which can be considered independently of other triggers, for
 * example by sending a message to the modmail when a user makes his first
 * loan.
 * 
 * @author Timothy
 */
public interface LoanTrigger {
	/**
	 * If marked true, the bot will fail to respond to the loan if there is an error
	 * while processing this trigger. If false, the bot will simply PM the modmail
	 * under the same circumstance.
	 * @return true if this trigger is essential, false otherwise
	 */
	public boolean essential();
	
	/**
	 * Called when a new valid load summon is created. May modify the responses
	 * of the loansbot using the given context. This is called before the loan
	 * is stored in the database but after currency conversion details have
	 * been dealt with (so the amount is accurate for USD)
	 *  
	 * @param ctx the information regarding the loan summon for the new loan
	 */
	public void onNewLoan(LoanSummonContext ctx);
}
