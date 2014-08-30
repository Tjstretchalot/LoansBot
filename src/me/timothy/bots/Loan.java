package me.timothy.bots;

/**
 * Represents, exactly, a database entry for a loan. Lender and
 * borrower are guaranteed not to be prefixed with /u/ for consistency
 * with checking database entries. This is done in the setter methods.
 * <br><br>
 * Lender and borrower are guaranteed to be lower-case and not-null
 *
 * 
 * @author Timothy
 */
public class Loan {
	private int id;
	
	private int amountPennies;
	private String lender;
	private String borrower;
	private int amountPaidPennies;
	private boolean unpaid;
	private long dateLoanGivenJUTC;
	private long datePaidBackFullJUTC;
	
	private String originalThread;
	
	/**
	 * Creates a loan
	 * 
	 * @param amountPennies the amount, in pennies, of the principal
	 * @param lender who lended the money (may be modified. See class comments)
	 * @param borrower who borrowed the money (may be modified. See class comments)
	 * @param amountPaidPennies the amount , in pennies, of the money paid back to the lender
	 * @param unpaid if the loan has been marked as unpaid by the lender
	 * @param dateLoanGivenJUTC when the loan was given, as if by a call to {@code System.currentTimeMillis}
	 * @param datePaidBackFullJUTC when the loan was paid back in full, as if by a call to {@code System.currentTimeMillis}
	 */
	public Loan(int amountPennies, String lender, String borrower,
			int amountPaidPennies, boolean unpaid, long dateLoanGivenJUTC, long datePaidBackFullJUTC) {
		setAmountPennies(amountPennies);
		setLender(lender);
		setBorrower(borrower);
		setAmountPaidPennies(amountPaidPennies);
		
		this.unpaid = unpaid;
		this.dateLoanGivenJUTC = dateLoanGivenJUTC;
		this.datePaidBackFullJUTC = datePaidBackFullJUTC;
	}
	
	/**
	 * @return amount of the initial loan, in pennies
	 */
	public int getAmountPennies() {
		return amountPennies;
	}
	
	/**
	 * Sets the amount of the initial loan, in pennies
	 * @param amountPennies the new amount of the initial loan
	 */
	public void setAmountPennies(int amountPennies) {
		this.amountPennies = amountPennies;
	}
	
	/**
	 * @return who gave the loan. Not-null, lower-case, and not prefixed with /u/
	 */
	public String getLender() {
		return lender;
	}
	
	/**
	 * @param lender the new lender
	 * @throws NullPointerException if lender is null
	 */
	public void setLender(String lender) {
		lender = lender.toLowerCase();
		if(lender.startsWith("/u/"))
			lender = lender.substring(3);
		
		this.lender = lender;
	}
	
	/**
	 * @return who borrowed the loan. Not-null, lower-case, and not prefixed with /u/
	 */
	public String getBorrower() {
		return borrower;
	}
	
	/**
	 * @param borrower the new borrower
	 * @throws NullPointerException if the borrower is null
	 */
	public void setBorrower(String borrower) {
		borrower = borrower.toLowerCase();
		if(borrower.startsWith("/u/"))
			borrower = borrower.substring(3);
		this.borrower = borrower;
	}
	
	/**
	 * Sets the amount paid, in pennies
	 * @return the amount paid back for the loan, in pennies
	 */
	public int getAmountPaidPennies() {
		return amountPaidPennies;
	}
	
	/**
	 * @param amountPaidPennies the amount paid back on this loan, in pennies
	 * @throws IllegalArgumentException if {@code amountPaidPennies} is less than 0
	 */
	public void setAmountPaidPennies(int amountPaidPennies) {
		if(amountPaidPennies < 0)
			throw new IllegalArgumentException("Negative amount paid");
		
		this.amountPaidPennies = amountPaidPennies;
	}

	/**
	 * @return the dateLoanGivenJUTC
	 */
	public long getDateLoanGivenJUTC() {
		return dateLoanGivenJUTC;
	}

	/**
	 * @return the datePaidBackFullJUTC
	 */
	public long getDatePaidBackFullJUTC() {
		return datePaidBackFullJUTC;
	}

	/**
	 * @return the id of the loan
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the id of the loan (in the database)
	 * @param id the id
	 */
	public void setId(int id) {
		this.id = id;
	}

	public String getOriginalThread() {
		return originalThread;
	}

	public void setOriginalThread(String originalThread) {
		this.originalThread = originalThread;
	}

	/**
	 * @return the unpaid
	 */
	public boolean isUnpaid() {
		return unpaid;
	}

	/**
	 * @param unpaid the unpaid to set
	 */
	public void setUnpaid(boolean unpaid) {
		this.unpaid = unpaid;
	}

	/**
	 * @param dateLoanGivenJUTC the dateLoanGivenJUTC to set
	 */
	public void setDateLoanGivenJUTC(long dateLoanGivenJUTC) {
		this.dateLoanGivenJUTC = dateLoanGivenJUTC;
	}

	/**
	 * @param datePaidBackFullJUTC the datePaidBackFullJUTC to set
	 */
	public void setDatePaidBackFullJUTC(long datePaidBackFullJUTC) {
		this.datePaidBackFullJUTC = datePaidBackFullJUTC;
	}
	
	
}
