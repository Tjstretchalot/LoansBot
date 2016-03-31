package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.Repayment;

/**
 * Describes a repayment mapping
 * 
 * @author Timothy
 */
public interface RepaymentMapping extends ObjectMapping<Repayment> {
	/**
	 * Fetches the repayment information for the specified loan.
	 * 
	 * @param loanId the loan to get repayments for 
	 * @return the repayments or an empty list
	 */
	public List<Repayment> fetchByLoanId(int loanId);
}
