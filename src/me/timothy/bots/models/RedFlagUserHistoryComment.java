package me.timothy.bots.models;

import java.sql.Timestamp;

import me.timothy.jreddit.info.Comment;

/**
 * Describes a comment that a user made that was parsed during a red flag
 * report. These are transient.
 *  
 * @author Timothy
 */
public class RedFlagUserHistoryComment {
	/** The id for the row */
	public int id;
	/** The id for the report that this comment was parsed for */
	public int reportId;
	/** The id of the person who made this comment */
	public int userId;
	/** The fullname of the comment */
	public String fullname;
	/** The permalink to the comment */
	public String permalink;
	/** The actual body of the comment */
	public String body;
	/** Which subreddit the comment was posted on */
	public String subreddit;
	/** When the comment was posted */
	public Timestamp createdAt;
	/** When the comment was most recently edited (null for never) */
	public Timestamp editedAt;
	
	/**
	 * Create a new red flag user history comment
	 * @param id the database id (-1 if not yet saved)
	 * @param reportId the red flag report that corresponds with this parsing
	 * @param personId the id of the person (in our database) who made this comment
	 * @param fullname the fullname of the comment
	 * @param permalink a permalink to the comment
	 * @param body the actual markup of the comment
	 * @param subreddit the subreddit the comment was put in
	 * @param createdAt when the comment was posted
	 * @param editedAt null if the comment was never edited, other the time when it was last edited
	 */
	public RedFlagUserHistoryComment(int id, int reportId, int personId, String fullname, String permalink, String body,
			String subreddit, Timestamp createdAt, Timestamp editedAt) {
		super();
		this.id = id;
		this.reportId = reportId;
		this.userId = personId;
		this.fullname = fullname;
		this.permalink = permalink;
		this.body = body;
		this.subreddit = subreddit;
		this.createdAt = createdAt;
		this.editedAt = editedAt;
	}
	
	/**
	 * Generate a new comment that is not in the database based on an actual reddit comment
	 * @param comment the reddit comment
	 * @param reportId the report this is for
	 * @param personId the person who made the comment
	 */
	public RedFlagUserHistoryComment(Comment comment, int reportId, int personId) {
		this(-1, reportId, personId, comment.fullname(), comment.permalink(), comment.body(), 
				comment.subreddit(), new Timestamp((long)(comment.createdUTC() * 1000)), 
				comment.edited() ? new Timestamp((long)(comment.editedTime() * 1000)) : null);
	}

	/**
	 * Determines if this comment is potentially valid without knowing anything about
	 * the database
	 * @return if this comment is potentially valid
	 */
	public boolean isValid() {
		return (reportId > 0 && userId > 0 && fullname != null && permalink != null && body != null 
				&& subreddit != null && createdAt != null);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((editedAt == null) ? 0 : editedAt.hashCode());
		result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
		result = prime * result + id;
		result = prime * result + ((permalink == null) ? 0 : permalink.hashCode());
		result = prime * result + userId;
		result = prime * result + reportId;
		result = prime * result + ((subreddit == null) ? 0 : subreddit.hashCode());
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
		RedFlagUserHistoryComment other = (RedFlagUserHistoryComment) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (editedAt == null) {
			if (other.editedAt != null)
				return false;
		} else if (!editedAt.equals(other.editedAt))
			return false;
		if (fullname == null) {
			if (other.fullname != null)
				return false;
		} else if (!fullname.equals(other.fullname))
			return false;
		if (id != other.id)
			return false;
		if (permalink == null) {
			if (other.permalink != null)
				return false;
		} else if (!permalink.equals(other.permalink))
			return false;
		if (userId != other.userId)
			return false;
		if (reportId != other.reportId)
			return false;
		if (subreddit == null) {
			if (other.subreddit != null)
				return false;
		} else if (!subreddit.equals(other.subreddit))
			return false;
		return true;
	}
}
