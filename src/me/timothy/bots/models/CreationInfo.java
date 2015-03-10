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
		REDDITLOANS(1)
		
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
		default:
			throw new IllegalArgumentException("Unknown creation type " + type.name());
		}
		
		return true;
	}
}
