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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
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
		ShareCode other = (ShareCode) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
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

	@Override
	public String toString() {
		return "ShareCode [id=" + id + ", userId=" + userId + ", code=" + code + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
	
	
}
