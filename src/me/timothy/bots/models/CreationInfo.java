package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes by which process a loan was created.
 * 
 * @author Timothy
 *
 */
public class CreationInfo {
	
	/**
	 * Wrapper around the creation type
	 * 
	 * @author Timothy
	 */
	public enum CreationType {
		/**
		 * A loan that was created on reddit
		 */
		REDDIT(0),
		
		/**
		 * A loan that was created on reddit loans
		 */
		REDDITLOANS(1),
		
		/**
		 * A loan that was created retroactively by a paid
		 * summon that has not yet found the loan command
		 */
		PAID_SUMMON(2)
		
		;
		
		/**
		 * The corresponding number in the type column
		 */
		private int typeNum;
		
		CreationType(int typeNum) {
			this.typeNum = typeNum;
		}
		
		public int getTypeNum() {
			return typeNum;
		}
		
		/**
		 * Gets the CreationType that corresponds to a particular
		 * type num.
		 * 
		 * @param typeNum the type num
		 * @return the corresponding creation type if it exists, otherwise null
		 */
		public static CreationType getByTypeNum(int typeNum) {
			for(CreationType ct : CreationType.values()) {
				if(ct.getTypeNum() == typeNum) {
					return ct;
				}
			}
			return null;
		}
	}
	
	public int id;
	public int loanId;
	public CreationType type;
	public String thread;
	public String reason;
	public int userId;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public CreationInfo(int id, int loanId, CreationType type, String thread, String reason, int userId, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.loanId = loanId;
		this.type = type;
		this.thread = thread;
		this.reason = reason;
		this.userId = userId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public CreationInfo() {
		this(-1, -1, null, null, null, -1, null, null);
	}

	/**
	 * Ensures this creation info is consistent, that is if
	 * type is REDDIT then reason is null and userId is <= 0;
	 * if type is REDDITLOANS then thread is null and userId > 0.
	 * 
	 * Also ensures createdAt and updatedAt are not null
	 * @return if this creation info is plausible
	 */
	public boolean isValid() {
		if(type == null || createdAt == null && updatedAt == null || loanId <= 0)
			return false;
		
		switch(type) {
		case REDDIT:
			if(thread == null || reason != null || userId > 0)
				return false;
			break;
		case REDDITLOANS:
			if(thread != null || reason == null || userId <= 0)
				return false;
			break;
		case PAID_SUMMON:
			break;
		default:
			throw new IllegalArgumentException("Unknown creation type " + type.name());
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + loanId;
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((thread == null) ? 0 : thread.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		CreationInfo other = (CreationInfo) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (loanId != other.loanId)
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (thread == null) {
			if (other.thread != null)
				return false;
		} else if (!thread.equals(other.thread))
			return false;
		if (type != other.type)
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
		return "CreationInfo [id=" + id + ", loanId=" + loanId + ", type=" + type + ", thread=" + thread + ", reason="
				+ reason + ", userId=" + userId + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
	
}
