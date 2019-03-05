package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a request to revisit vetting a user after a certain number of loans.
 * 
 * @author Timothy
 */
public class DelayedVettingRequest {
	/** Row id for this entry, -1 if not in database yet */
	public int id;
	/** The user id to revisit */
	public int userId;
	/** The number of loans before which we revisit */
	public int numberLoans;
	/** The reason we delayed vetting for this user */
	public String reason;
	/** The time when the request to delay was made */
	public Timestamp createdAt;
	/** When we rerequested vetting for this user, or null if not yet */
	public Timestamp rerequestedAt;
	
	/**
	 * @param id row id or -1 if not in the database
	 * @param userId id of the user to request vetting for later
	 * @param numberLoans the number of loans made by the user at which we should request vetting again
	 * @param reason the reason we didnt just trust them the first time
	 * @param createdAt when we made the request to delay
	 * @param rerequestedAt when we fulfilled the request or null if it hasn't been fulfilled yet
	 */
	public DelayedVettingRequest(int id, int userId, int numberLoans, String reason, Timestamp createdAt,
			Timestamp rerequestedAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.numberLoans = numberLoans;
		this.reason = reason;
		this.createdAt = createdAt;
		this.rerequestedAt = rerequestedAt;
	}
	
	/**
	 * Determines if this is a potentially valid entry
	 * @return true if this passes a sniff test, false otherwise
	 */
	public boolean isValid() {
		return userId > 0 && numberLoans > 0 && reason != null && createdAt != null && (rerequestedAt == null || rerequestedAt.after(createdAt));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + numberLoans;
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((rerequestedAt == null) ? 0 : rerequestedAt.hashCode());
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
		DelayedVettingRequest other = (DelayedVettingRequest) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (numberLoans != other.numberLoans)
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (rerequestedAt == null) {
			if (other.rerequestedAt != null)
				return false;
		} else if (!rerequestedAt.equals(other.rerequestedAt))
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "DelayedVettingRequest [id=" + id + ", userId=" + userId + ", numberLoans=" + numberLoans + ", reason="
				+ reason + ", createdAt=" + createdAt + ", rerequestedAt=" + rerequestedAt + "]";
	}
}
