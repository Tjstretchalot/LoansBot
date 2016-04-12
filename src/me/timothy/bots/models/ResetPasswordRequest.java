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
	
	public ResetPasswordRequest() {
		this(-1, -1, null, false, false, null, null);
	}

	/**
	 * Checks if this could potentially be a valid
	 * entry
	 * @return if this request is probably valid
	 */
	public boolean isValid() {
		return userId > 0 && userId > 0 && resetCode != null && createdAt != null && updatedAt != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((resetCode == null) ? 0 : resetCode.hashCode());
		result = prime * result + (resetCodeSent ? 1231 : 1237);
		result = prime * result + (resetCodeUsed ? 1231 : 1237);
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
		ResetPasswordRequest other = (ResetPasswordRequest) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (resetCode == null) {
			if (other.resetCode != null)
				return false;
		} else if (!resetCode.equals(other.resetCode))
			return false;
		if (resetCodeSent != other.resetCodeSent)
			return false;
		if (resetCodeUsed != other.resetCodeUsed)
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
		return "ResetPasswordRequest [id=" + id + ", userId=" + userId + ", resetCode=" + resetCode + ", resetCodeSent="
				+ resetCodeSent + ", resetCodeUsed=" + resetCodeUsed + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}
	
}
