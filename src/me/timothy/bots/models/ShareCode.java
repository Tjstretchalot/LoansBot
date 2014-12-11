package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a share code
 * 
 * @author Timothy
 */
public class ShareCode {
	public int id;
	public int userId;
	public String code;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public ShareCode(int id, int userId, String code, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.userId = userId;
		this.code = code;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public ShareCode() {
		this(-1, -1, null, null, null);
	}
	
	/**
	 * Validates if the user id, code is not null,
	 * created at and updated at is not null
	 * 
	 * @return if this share code is not obviously wrong
	 */
	public boolean isValid() {
		if(userId < 0)
			return false;
		else if(code == null)
			return false;
		else if(createdAt == null)
			return false;
		else if(updatedAt == null)
			return false;
		return true;
	}
}
