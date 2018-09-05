package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Marks a spot in the queue for a username to have his history scanned
 * and red flags reported. If the queue is moving slowly a single user
 * may have multiple spots in the red flag queue; simply take all of them
 * off the queue once you complete one
 * 
 * @author Timothy
 */
public class RedFlagQueueSpot {
	/**
	 * Database identifier for this red flag history
	 */
	public int id;
	
	/**
	 * The id for the report that this queue is generating. This corresponds with
	 * RedFlagReport - null if we haven't generated one yet
	 */
	public Integer reportId;
	
	/**
	 * The id of the username that we are scanning.
	 */
	public int usernameId;
	
	/**
	 * When we first queued this red flag check 
	 */
	public Timestamp createdAt;
	
	/**
	 * When we first started working on this red flag check
	 */
	public Timestamp startedAt;
	
	/**
	 * When we finished this red flag check
	 */
	public Timestamp completedAt;

	/**
	 * @param id the database identifier for this queue spot or -1 if not in the database yet
	 * @param reportId the id for the report that has been generated for this or null if not generaetd yet
	 * @param usernameId the id of the username that we have queued
	 * @param createdAt when this spot was created
	 * @param startedAt when we started working on this spot or null if still not started
	 * @param completedAt when this spot completed or null if still not completed
	 */
	public RedFlagQueueSpot(int id, Integer reportId, int usernameId,
			Timestamp createdAt, Timestamp startedAt, Timestamp completedAt) {
		super();
		this.id = id;
		this.reportId = reportId;
		this.usernameId = usernameId;
		this.createdAt = createdAt;
		this.startedAt = startedAt;
		this.completedAt = completedAt;
	}
	
	/**
	 * Determine if this can reasonably be saved into the database
	 * @return if this is complete-ish
	 */
	public boolean isValid() {
		return (reportId == null || reportId > 0) 
				&& (usernameId > 0) 
				&& (createdAt != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((completedAt == null) ? 0 : completedAt.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((reportId == null) ? 0 : reportId.hashCode());
		result = prime * result + ((startedAt == null) ? 0 : startedAt.hashCode());
		result = prime * result + usernameId;
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
		RedFlagQueueSpot other = (RedFlagQueueSpot) obj;
		if (completedAt == null) {
			if (other.completedAt != null)
				return false;
		} else if (!completedAt.equals(other.completedAt))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (reportId == null) {
			if (other.reportId != null)
				return false;
		} else if (!reportId.equals(other.reportId))
			return false;
		if (startedAt == null) {
			if (other.startedAt != null)
				return false;
		} else if (!startedAt.equals(other.startedAt))
			return false;
		if (usernameId != other.usernameId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RedFlagQueueSpot [id=" + id + ", reportId=" + reportId + ", usernameId=" + usernameId + ", createdAt="
				+ createdAt + ", startedAt=" + startedAt + ", completedAt=" + completedAt + "]";
	}
	
	
}
