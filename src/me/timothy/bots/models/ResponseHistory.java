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
	
	/**
	 * Check to make sure this response history is plausible
	 * 
	 * @return
	 */
	public boolean isValid() {
		return responseId > 0 && userId > 0 && oldRaw != null && newRaw != null && reason != null && createdAt != null && updatedAt != null;
	}
}
