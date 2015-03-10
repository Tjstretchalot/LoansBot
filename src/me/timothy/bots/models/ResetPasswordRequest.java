package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * <p>Describes a request to reset your password. It should be noted
 * that this is not necessarily an <i>active</i> request, since requests
 * are stored indefinitely in order to track trends.</p>
 * <p>Reset password requests are sent to the reddit account that is
 * associated with the user. This presents no additional security risk,
 * since you can perform any commands that could normally be performed
 * solely through using that reddit account.</p>
 * 
 * @author Timothy
 */
public class ResetPasswordRequest {
	public int id;
	public int userId;
	public String resetCode;
	public boolean resetCodeSent;
	public boolean resetCodeUsed;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public ResetPasswordRequest(int id, int userId, String resetCode, boolean resetCodeSent, boolean resetCodeUsed, Timestamp createdAt,
			Timestamp updatedAt) {
		this.id = id;
		this.userId = userId;
		this.resetCode = resetCode;
		this.resetCodeSent = resetCodeSent;
		this.resetCodeUsed = resetCodeUsed;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	/**
	 * Checks if this could potentially be a valid
	 * entry
	 * @return if this request is probably valid
	 */
	public boolean isValid() {
		return id > 0 && userId > 0 && resetCode != null && createdAt != null && updatedAt != null;
	}
}
