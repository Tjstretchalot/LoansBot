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
}
