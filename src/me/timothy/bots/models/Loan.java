package me.timothy.bots.models;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Describes a loan in the database
 * 
 * @author Timothy
 */
public class Loan {
	public int id;
	public int lenderId;
	public int borrowerId;
	public int principalCents;
	public int principalRepaymentCents;
	public boolean unpaid;
	public boolean deleted;
	public String deletedReason;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	public Timestamp deletedAt;
	
	public Loan(int id, int lenderId, int borrowerId, int principalCents, int principalRepaymentCents, boolean unpaid, 
			boolean deleted, String deletedReason, Timestamp createdAt, Timestamp updatedAt, Timestamp deletedAt) {
		super();
		this.id = id;
		this.lenderId = lenderId;
		this.borrowerId = borrowerId;
		this.principalCents = principalCents;
		this.principalRepaymentCents = principalRepaymentCents;
		this.unpaid = unpaid;
		this.deleted = deleted;
		this.deletedReason = deletedReason;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
	
	public Loan() {
		this(-1, -1, -1, -1, -1, false, false, null, new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()), null);
	}
	
	/**
	 * Ensures the lender id and borrower id are set, principal cents and principal repayment cents are positive,
	 * and that created at and updated at are not null
	 * 
	 * @return if this loan isn't obviously wrong
	 */
	public boolean isValid() {
		if(lenderId <= 0)
			return false;
		else if(borrowerId <= 0)
			return false;
		else if(principalCents < 0)
			return false;
		else if(principalRepaymentCents < 0)
			return false;
		else if(createdAt == null)
			return false;
		else if(updatedAt == null)
			return false;
		else
			return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + borrowerId;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + (deleted ? 1231 : 1237);
		result = prime * result + ((deletedAt == null) ? 0 : deletedAt.hashCode());
		result = prime * result + ((deletedReason == null) ? 0 : deletedReason.hashCode());
		result = prime * result + id;
		result = prime * result + lenderId;
		result = prime * result + principalCents;
		result = prime * result + principalRepaymentCents;
		result = prime * result + (unpaid ? 1231 : 1237);
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
		Loan other = (Loan) obj;
		if (borrowerId != other.borrowerId)
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (deleted != other.deleted)
			return false;
		if (deletedAt == null) {
			if (other.deletedAt != null)
				return false;
		} else if (!deletedAt.equals(other.deletedAt))
			return false;
		if (deletedReason == null) {
			if (other.deletedReason != null)
				return false;
		} else if (!deletedReason.equals(other.deletedReason))
			return false;
		if (id != other.id)
			return false;
		if (lenderId != other.lenderId)
			return false;
		if (principalCents != other.principalCents)
			return false;
		if (principalRepaymentCents != other.principalRepaymentCents)
			return false;
		if (unpaid != other.unpaid)
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Loan [id=" + id + ", lenderId=" + lenderId + ", borrowerId=" + borrowerId + ", principalCents="
				+ principalCents + ", principalRepaymentCents=" + principalRepaymentCents + ", unpaid=" + unpaid
				+ ", deleted=" + deleted + ", deletedReason=" + deletedReason + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", deletedAt=" + deletedAt + "]";
	}
	
	
}
