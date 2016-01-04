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
}
