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

	public AdminUpdate() {
		this(-1, -1, -1, null, -1, -1, -1, -1, false, false, null, -1, -1, -1, -1, false, false, null, null, null);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + loanId;
		result = prime * result + newBorrowerId;
		result = prime * result + (newDeleted ? 1231 : 1237);
		result = prime * result + ((newDeletedReason == null) ? 0 : newDeletedReason.hashCode());
		result = prime * result + newLenderId;
		result = prime * result + newPrincipalCents;
		result = prime * result + newPrincipalRepaymentCents;
		result = prime * result + (newUnpaid ? 1231 : 1237);
		result = prime * result + oldBorrowerId;
		result = prime * result + (oldDeleted ? 1231 : 1237);
		result = prime * result + ((oldDeletedReason == null) ? 0 : oldDeletedReason.hashCode());
		result = prime * result + oldLenderId;
		result = prime * result + oldPrincipalCents;
		result = prime * result + oldPrincipalRepaymentCents;
		result = prime * result + (oldUnpaid ? 1231 : 1237);
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		result = prime * result + userId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdminUpdate other = (AdminUpdate) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (loanId != other.loanId)
			return false;
		if (newBorrowerId != other.newBorrowerId)
			return false;
		if (newDeleted != other.newDeleted)
			return false;
		if (newDeletedReason == null) {
			if (other.newDeletedReason != null)
				return false;
		} else if (!newDeletedReason.equals(other.newDeletedReason))
			return false;
		if (newLenderId != other.newLenderId)
			return false;
		if (newPrincipalCents != other.newPrincipalCents)
			return false;
		if (newPrincipalRepaymentCents != other.newPrincipalRepaymentCents)
			return false;
		if (newUnpaid != other.newUnpaid)
			return false;
		if (oldBorrowerId != other.oldBorrowerId)
			return false;
		if (oldDeleted != other.oldDeleted)
			return false;
		if (oldDeletedReason == null) {
			if (other.oldDeletedReason != null)
				return false;
		} else if (!oldDeletedReason.equals(other.oldDeletedReason))
			return false;
		if (oldLenderId != other.oldLenderId)
			return false;
		if (oldPrincipalCents != other.oldPrincipalCents)
			return false;
		if (oldPrincipalRepaymentCents != other.oldPrincipalRepaymentCents)
			return false;
		if (oldUnpaid != other.oldUnpaid)
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AdminUpdate [id=" + id + ", loanId=" + loanId + ", userId=" + userId + ", reason=" + reason
				+ ", oldLenderId=" + oldLenderId + ", oldBorrowerId=" + oldBorrowerId + ", oldPrincipalCents="
				+ oldPrincipalCents + ", oldPrincipalRepaymentCents=" + oldPrincipalRepaymentCents + ", oldUnpaid="
				+ oldUnpaid + ", oldDeleted=" + oldDeleted + ", oldDeletedReason=" + oldDeletedReason + ", newLenderId="
				+ newLenderId + ", newBorrowerId=" + newBorrowerId + ", newPrincipalCents=" + newPrincipalCents
				+ ", newPrincipalRepaymentCents=" + newPrincipalRepaymentCents + ", newUnpaid=" + newUnpaid
				+ ", newDeleted=" + newDeleted + ", newDeletedReason=" + newDeletedReason + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
	
	
}
