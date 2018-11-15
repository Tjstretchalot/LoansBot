package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a user which is blacklisted from promotions (ie. invitations to
 * the lenderscamp). Such users are still allowed to interact with the normal
 * features of the bot.
 * 
 * @author Timothy
 */
public class PromotionBlacklist {
	/** The row id */
	public int id;
	/** The id of the person on the promotion blacklist */
	public int userId;
	/** The moderator which added this user to the promotion blacklist */
	public int modUserId;
	/** The reason this user was added to the promotion blacklist */
	public String reason;
	/** When this person was added to the promotion blacklist */
	public Timestamp addedAt;
	/** When this entry no longer became active */
	public Timestamp removedAt;
	
	/**
	 * @param id the row id or -1 if not yet in the database
	 * @param userId the id of the user on the blacklist
	 * @param modUserId the id of the moderator who added the person to the blacklist
	 * @param reason the reason the person was added to the blacklist
	 * @param addedAt when the person was added to the blacklist
	 * @param removedAt when this entry became invalid.
	 */
	public PromotionBlacklist(int id, int userId, int modUserId, String reason, Timestamp addedAt, Timestamp removedAt) {
		this.id = id;
		this.userId = userId;
		this.modUserId = modUserId;
		this.reason = reason;
		this.addedAt = addedAt;
		this.removedAt = removedAt;
	}
	
	/**
	 * If this is a potentially viable entry to save to the database
	 * @return if this is maybe valid
	 */
	public boolean isValid() {
		return (userId > 0 && modUserId > 0 && reason != null && addedAt != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addedAt == null) ? 0 : addedAt.hashCode());
		result = prime * result + id;
		result = prime * result + modUserId;
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((removedAt == null) ? 0 : removedAt.hashCode());
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
		PromotionBlacklist other = (PromotionBlacklist) obj;
		if (addedAt == null) {
			if (other.addedAt != null)
				return false;
		} else if (!addedAt.equals(other.addedAt))
			return false;
		if (id != other.id)
			return false;
		if (modUserId != other.modUserId)
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (removedAt == null) {
			if (other.removedAt != null)
				return false;
		} else if (!removedAt.equals(other.removedAt))
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PromotionBlacklist [id=" + id + ", userId=" + userId + ", modUserId=" + modUserId + ", reason=" + reason
				+ ", addedAt=" + addedAt + ", removedAt=" + removedAt + "]";
	}
	
}
