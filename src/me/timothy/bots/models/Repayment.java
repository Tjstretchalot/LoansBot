package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a repayment
 * 
 * @author Timothy
 */
public class Repayment {
	public int id;
	public int loanId;
	public int amountCents;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public Repayment(int id, int loanId, int amountCents, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.loanId = loanId;
		this.amountCents = amountCents;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public Repayment() {
		this(-1, -1, -1, null, null);
	}
	
	/**
	 * Verifies the loan is positive, amount cents is positive,
	 * and that created at and updated at is not null
	 * 
	 * @return if this repayment is not obviously wrong
	 */
	public boolean isValid() {
		if(loanId < 0)
			return false;
		else if(amountCents < 0)
			return false;
		else if(createdAt == null)
			return false;
		else if(updatedAt == null)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amountCents;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + loanId;
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
		Repayment other = (Repayment) obj;
		if (amountCents != other.amountCents)
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (loanId != other.loanId)
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
		return "Repayment [id=" + id + ", loanId=" + loanId + ", amountCents=" + amountCents + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
	
}
