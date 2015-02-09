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
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public Loan(int id, int lenderId, int borrowerId, int principalCents, int principalRepaymentCents, boolean unpaid, 
			Timestamp createdAt, Timestamp updatedAt) {
		super();
		this.id = id;
		this.lenderId = lenderId;
		this.borrowerId = borrowerId;
		this.principalCents = principalCents;
		this.principalRepaymentCents = principalRepaymentCents;
		this.unpaid = unpaid;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public Loan() {
		this(-1, -1, -1, -1, -1, false, new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
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
}
