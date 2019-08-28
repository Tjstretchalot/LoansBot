package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Allows users to opt out of recieving a pm when a borrower with which they
 * have an active loan makes a request thread.
 * 
 * @author Timothy
 */
public class BorrowerReqPMOptOut {
	/** The database row identifier **/
	public int id;
	/** The id of the user opting out */
	public int userId;
	/** When the user opted out */
	public Timestamp createdAt;
	
	/**
	 * @param id the database row identifier or -1 if not in the database
	 * @param userId the id of the user which is opting out
	 * @param createdAt when the user opted out
	 */
	public BorrowerReqPMOptOut(int id, int userId, Timestamp createdAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.createdAt = createdAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + userId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BorrowerReqPMOptOut))
			return false;
		BorrowerReqPMOptOut other = (BorrowerReqPMOptOut) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BorrowerReqPMOptOut [id=" + id + ", userId=" + userId + ", createdAt=" + createdAt + "]";
	}
}
