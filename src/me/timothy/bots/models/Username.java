package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Since it is not uncommon for people to create multiple accounts,
 * and this is not strictly against the rules (unless the other account
 * has broken some rule), it is appropriate to have a way of sharing
 * history between multiple users. This is done by allowing one user to
 * have-many usernames.
 * 
 * @author Tmoor
 *
 */
public class Username {
	public int id;
	public int userId;
	public String username;
	
	public Timestamp createdAt;
	public Timestamp updatedAt;
	/**
	 * Creates a username with all information
	 * @param id the id
	 * @param userId the user id
	 * @param username the username
	 * @param createdAt when this was created
	 * @param updatedAt when this was last updated
	 */
	public Username(int id, int userId, String username, Timestamp createdAt, Timestamp updatedAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	/**
	 * Creates a username with sensible defaults that will fail
	 * isValid
	 */
	public Username() {
		this(-1, -1, null, null, null);
	}
	
	/**
	 * Verifies the username, createdAt, and updatedAt fields
	 * are non-null
	 * 
	 * @return if this username could theoretically be valid
	 */
	public boolean isValid() {
		if(username == null)
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
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		result = prime * result + userId;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		Username other = (Username) obj;
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
		if (userId != other.userId)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Username [id=" + id + ", userId=" + userId + ", username=" + username + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
	
	
}
