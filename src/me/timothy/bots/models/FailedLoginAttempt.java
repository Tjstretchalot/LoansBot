package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a saved login attempt
 * 
 * @author Timothy
 */
public class FailedLoginAttempt {
	/** Database row identifier */
	public int id;
	/** The username that they tried to login with */
	public String username;
	/** When they attempted to login */
	public Timestamp attemptedAt;
	
	/**
	 * @param id database row identifier or -1 if not in database yet
	 * @param username the username they tried to login with
	 * @param attemptedAt when they attempted to login
	 */
	public FailedLoginAttempt(int id, String username, Timestamp attemptedAt) {
		super();
		this.id = id;
		this.username = username;
		this.attemptedAt = attemptedAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attemptedAt == null) ? 0 : attemptedAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FailedLoginAttempt))
			return false;
		FailedLoginAttempt other = (FailedLoginAttempt) obj;
		if (attemptedAt == null) {
			if (other.attemptedAt != null)
				return false;
		} else if (!attemptedAt.equals(other.attemptedAt))
			return false;
		if (id != other.id)
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
		return "FailedLoginAttempt [id=" + id + ", username=" + username + ", attemptedAt=" + attemptedAt + "]";
	}
	
	
}
