package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Responses are very flimsy; so we keep a good
 * history on them. This describes a single point
 * of history; responses can have many of them
 * 
 * @author Timothy
 */
public class ResponseHistory {
	public int id;
	public int responseId;
	public int userId;
	public String oldRaw;
	public String newRaw;
	public String reason;
	
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public ResponseHistory(int id, int responseId, int userId, String oldRaw, String newRaw, String reason, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.responseId = responseId;
		this.userId = userId;
		this.oldRaw = oldRaw;
		this.newRaw = newRaw;
		this.reason = reason;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public ResponseHistory() {
		this(-1, -1, -1, null, null, null, null, null);
	}

	/**
	 * Check to make sure this response history is plausible
	 * 
	 * @return if the response history is valid
	 */
	public boolean isValid() {
		return responseId > 0 && userId > 0 && oldRaw != null && newRaw != null && reason != null && createdAt != null && updatedAt != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((newRaw == null) ? 0 : newRaw.hashCode());
		result = prime * result + ((oldRaw == null) ? 0 : oldRaw.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + responseId;
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
		ResponseHistory other = (ResponseHistory) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (newRaw == null) {
			if (other.newRaw != null)
				return false;
		} else if (!newRaw.equals(other.newRaw))
			return false;
		if (oldRaw == null) {
			if (other.oldRaw != null)
				return false;
		} else if (!oldRaw.equals(other.oldRaw))
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (responseId != other.responseId)
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

	@Override
	public String toString() {
		return "ResponseHistory [id=" + id + ", responseId=" + responseId + ", userId=" + userId + ", oldRaw=" + oldRaw
				+ ", newRaw=" + newRaw + ", reason=" + reason + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ "]";
	}
	
	
}
