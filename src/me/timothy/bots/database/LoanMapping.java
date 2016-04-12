package me.timothy.bots.database;

import java.sql.Timestamp;
import java.util.List;

import me.timothy.bots.models.Loan;

/**
 * Describes a loan mapping
 * 
 * @author Timothy
 */
public interface LoanMapping extends ObjectMapping<Loan> {
	/**
	 * Fetches all user ids that have had a new loan since the specified timestamp
	 * @param timestamp the timestamp to check since
	 * @return user ids with a loan since the specified timestamp as lender
	 */
	public List<Integer> fetchLenderIdsWithNewLoanSince(Timestamp timestamp);
	
	/**
	 * Fetches loans that have a borrower that is the specified borrower id, or 
	 * a lender with a specified lender id. If {@code strict=true}, the loan must 
	 * have <i>both</i> the specified borrower and the specified lender
	 * 
	 * @param borrowerId the borrower id
	 * @param lenderId the lender id
	 * @param strict if the borrower <i>and</i> lender should match
	 * @return loans with the specified borrower and/or lender or an empty list
	 */
	public List<Loan> fetchWithBorrowerAndOrLender(int borrowerId, int lenderId, boolean strict);
	
	/**
	 * Fetches the number of loans with the specified user as the lender. Does <i>not</i>
	 * count deleted loans
	 * 
	 * @param lenderId the lender id
	 * @return the number of loans with the specified user as the lender
	 */
	public int fetchNumberOfLoansWithUserAsLender(int lenderId);
}
