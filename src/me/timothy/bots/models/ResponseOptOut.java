package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Users may opt out of the bot automatically responding to non-request posts using 
 * this model.
 * 
 * @author Timothy
 */
public class ResponseOptOut {
	/** The database row identifier or -1 if not yet in the database */
	public int id;
	/** The id of the user who opted out */
	public int userId;
	/** When the user opted out */
	public Timestamp createdAt;
	
	/**
	 * @param id
	 * @param userId
	 * @param createdAt
	 */
	public ResponseOptOut(int id, int userId, Timestamp createdAt) {
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
		if (!(obj instanceof ResponseOptOut))
			return false;
		ResponseOptOut other = (ResponseOptOut) obj;
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
		return "ResponseOptOut [id=" + id + ", userId=" + userId + ", createdAt=" + createdAt + "]";
	}
}
