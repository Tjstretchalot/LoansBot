package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * An item in banned_users, these are users who have been 
 * banned by the loansbot from its respective subreddits.
 * 
 * @author Timothy
 */
public class BannedUser {
	/**
	 * The identifier for this banned user
	 * mapping
	 */
	public int id;
	
	/**
	 * The id of the user that was banned
	 */
	public int userID;
	
	/**
	 * When this banned user was added to the mapping
	 */
	public Timestamp createdAt;
	
	/**
	 * The last time we modified this bannedUser
	 */
	public Timestamp updatedAt;
	
	/**
	 * Initialize a new BannedUser with the specified id or user id.
	 * If the id hasn't been acquired yet, use -1.
	 * 
	 * @param id the id
	 * @param userID the user id
	 * @param createdAt when this was created
	 * @param updatedAt when this was last updated
	 */
	public BannedUser(int id, int userID, Timestamp createdAt, Timestamp updatedAt)
	{
		this.id = id;
		this.userID = userID;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	/**
	 * Determines if this banned user mapping is potentially valid.
	 * 
	 * @return if this is potentially valid
	 */
	public boolean isValid() {
		return userID > 0 && createdAt != null && updatedAt != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		result = prime * result + userID;
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
		BannedUser other = (BannedUser) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		if (userID != other.userID)
			return false;
		return true;
	}
}
