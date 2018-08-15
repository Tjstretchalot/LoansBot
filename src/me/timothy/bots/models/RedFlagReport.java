package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * This model serves as a centralized location to search for red flag reports
 * @author Timothy
 *
 */
public class RedFlagReport {
	/**
	 * Database identifier
	 */
	public int id;
	
	/**
	 * The username that was/is being reported on
	 */
	public int usernameId;
	
	/**
	 * We search a username from newest to oldest; this is the oldest thing fullname
	 * that we have already checked (can be passed to 
	 * {@link me.timothy.jreddit.RedditUtils#getUserHistory getUserHistory}
	 */
	public String afterFullname;
	
	/**
	 * When this database item was created
	 */
	public Timestamp createdAt;
	
	/**
	 * When we started working on this report
	 */
	public Timestamp startedAt;
	
	/**
	 * When we completed this report
	 */
	public Timestamp completedAt;

	/**
	 * @param id the database identifier for the report or -1 if not in the database yet
	 * @param usernameId the id of the username this report regards
	 * @param afterFullname the fullname of the latest thing from the users history we have already seen
	 * @param createdAt when we created this report
	 * @param startedAt when we started working on this report
	 * @param completedAt when we completed this report.
	 */
	public RedFlagReport(int id, int usernameId, String afterFullname, Timestamp createdAt, Timestamp startedAt,
			Timestamp completedAt) {
		super();
		this.id = id;
		this.usernameId = usernameId;
		this.afterFullname = afterFullname;
		this.createdAt = createdAt;
		this.startedAt = startedAt;
		this.completedAt = completedAt;
	}
	
	/**
	 * Determines if this red flag report is theoretically valid
	 * @return if this has all the important parts
	 */
	public boolean isValid() {
		return (usernameId > 0 && createdAt != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((afterFullname == null) ? 0 : afterFullname.hashCode());
		result = prime * result + ((completedAt == null) ? 0 : completedAt.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
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
		if (!(obj instanceof RedFlagReport))
			return false;
		RedFlagReport other = (RedFlagReport) obj;
		if (afterFullname == null) {
			if (other.afterFullname != null)
				return false;
		} else if (!afterFullname.equals(other.afterFullname))
			return false;
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
		return "RedFlagReport [id=" + id + ", usernameId=" + usernameId + ", afterFullname=" + afterFullname
				+ ", createdAt=" + createdAt + ", startedAt=" + startedAt + ", completedAt=" + completedAt + "]";
	}
}
