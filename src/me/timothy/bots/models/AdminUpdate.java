package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Only admins may update loans directly on the site, but this
 * table keeps track of those updates and makes them reversible
 * just in case.
 * 
 * @author Timothy
 *
 */
public class AdminUpdate {
	public int id;
	public int loanId;
	public int userId;
	public String reason;
	
	public int oldLenderId;
	public int oldBorrowerId;
	public int oldPrincipalCents;
	public int oldPrincipalRepaymentCents;
	public boolean oldUnpaid;
	public boolean oldDeleted;
	public String oldDeletedReason;
	
	public int newLenderId;
	public int newBorrowerId;
	public int newPrincipalCents;
	public int newPrincipalRepaymentCents;
	public boolean newUnpaid;
	public boolean newDeleted;
	public String newDeletedReason;
	
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public AdminUpdate(int id, int loanId, int userId, String reason, int oldLenderId, int oldBorrowerId,
			int oldPrincipalCents, int oldPrincipalRepaymentCents, boolean oldUnpaid, boolean oldDeleted,
			String oldDeletedReason, int newLenderId, int newBorrowerId, int newPrincipalCents,
			int newPrincipalRepaymentCents, boolean newUnpaid, boolean newDeleted, String newDeletedReason,
			Timestamp createdAt, Timestamp updatedAt) {
		super();
		this.id = id;
		this.loanId = loanId;
		this.userId = userId;
		this.reason = reason;
		this.oldLenderId = oldLenderId;
		this.oldBorrowerId = oldBorrowerId;
		this.oldPrincipalCents = oldPrincipalCents;
		this.oldPrincipalRepaymentCents = oldPrincipalRepaymentCents;
		this.oldUnpaid = oldUnpaid;
		this.oldDeleted = oldDeleted;
		this.oldDeletedReason = oldDeletedReason;
		this.newLenderId = newLenderId;
		this.newBorrowerId = newBorrowerId;
		this.newPrincipalCents = newPrincipalCents;
		this.newPrincipalRepaymentCents = newPrincipalRepaymentCents;
		this.newUnpaid = newUnpaid;
		this.newDeleted = newDeleted;
		this.newDeletedReason = newDeletedReason;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Checks if this admin update loan history
	 * could potentially be valid
	 * @return is this valid
	 */
	public boolean isValid() {
		return loanId > 0 && userId > 0 && reason != null && reason.length() > 5 && oldLenderId > 0 && oldBorrowerId > 0
				&& newLenderId > 0 && newBorrowerId > 0 && createdAt != null && updatedAt != null;
	}
}
