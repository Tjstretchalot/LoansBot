package me.timothy.bots.models;

import java.sql.Timestamp;
import java.util.Date;

/**
 * This describes contributors to the subreddit /r/lenderscamp.
 *  
 * @author Timothy
 */
public class LendersCampContributor {
	public int id;
	public int userId;
	public boolean botAdded;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public LendersCampContributor(int id, int userId, boolean botAdded, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.userId = userId;
		this.botAdded = botAdded;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public LendersCampContributor() {
		this(-1, -1, false, new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
	}

	@Override
	public String toString() {
		return "LendersCampContributor [id=" + id + ", botAdded=" + botAdded + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
	
	/**
	 * Ensures the userId is nonzero and positive, and the timestamps are non-null
	 * 
	 * @return if this model is plausibly valid
	 */
	public boolean isValid() {
		if(userId <= 0)
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
		result = prime * result + (botAdded ? 1231 : 1237);
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
		LendersCampContributor other = (LendersCampContributor) obj;
		if (botAdded != other.botAdded)
			return false;
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
		return true;
	}
	
	
}
