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
	 * Fetch the loan with the given id in the database if it exists, otherwise
	 * returns null.
	 * 
	 * @param id the database identifier for the desired loan
	 * @return the loan with the given identifier if it exists, null otherwise
	 */
	public Loan fetchByID(int id);
	
	/**
	 * Fetches all user ids that have had a new loan since the specified timestamp
	 * @param timestamp the timestamp to check since
	 * @return user ids with a loan since the specified timestamp as lender
	 */
	public List<Integer> fetchLenderIdsWithNewLoanSince(Timestamp timestamp);
	
	/**
	 * <p>Fetches loans that have a borrower that is the specified borrower id, or 
	 * a lender with a specified lender id. If {@code strict=true}, the loan must 
	 * have <i>both</i> the specified borrower and the specified lender.</p>
	 * 
	 * <p>Does not fetch deleted loans</p>
	 * 
	 * @param borrowerId the borrower id
	 * @param lenderId the lender id
	 * @param strict if the borrower <i>and</i> lender should match
	 * @return loans with the specified borrower and/or lender or an empty list
	 */
	public List<Loan> fetchWithBorrowerAndOrLender(int borrowerId, int lenderId, boolean strict);
	
	/**
	 * Fetches the number of loans with the specified user as the lender. Does <i>not</i>
	 * count deleted loans. This does not require that the loans were completed.
	 * 
	 * @param lenderId the lender id
	 * @return the number of loans with the specified user as the lender
	 */
	public int fetchNumberOfLoansWithUserAsLender(int lenderId);
	
	/**
	 * Fetches two integers, the first corresponds to the number of loans with the specified
	 * user as the lender, except deleted loans. The second corresponds to the number of 
	 * loans with the specified user as the lender which have a principal repayment equal
	 * to the original principal, not counting deleted loans.
	 * 
	 * @param lenderId the id of the lender you are interested in
	 * @return [loans by that lender, loans completed by that lender]
	 */
	public int[] fetchNumberOfLoansCompletedWithUserAsLender(int lenderId);
	
	/**
	 * Fetches the number of milliseconds that have elapsed since the oldest repayment
	 * for the given lender. Returns long max value if there are no loans repaid by
	 * the lender.
	 * 
	 * @param lenderId the id of the lender
	 * @return the number of milliseconds since the oldest $paid by that lender
	 */
	public long fetchTimeSinceEarliestRepaidLoan(int lenderId);

	/**
	 * Fetches the number of loans with the specified user as the borrower. Does <i>not</i>
	 * count deleted loans. This does not require that the loans were completed.
	 * 
	 * @param borrowerId the borrower id
	 * @return the number of undeleted loans with the given borrower
	 */
	public int fetchNumberOfLoansWithUserAsBorrower(int borrowerId);

	/**
	 * Fetches the number of outstanding loans with the specified user as the borrower. Does
	 * <i>not</i> count deleted loans. An outstanding loan is one which is not marked unpaid
	 * and has a principal repayment below the principal.
	 * 
	 * @param borrowerId the borrower id
	 * @return the number of outstanding loans with that user as a borrower
	 */
	public int fetchNumberOfOutstandingLoansWithUserAsBorrower(int borrowerId);
}
