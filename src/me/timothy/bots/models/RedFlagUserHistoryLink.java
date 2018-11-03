package me.timothy.bots.models;

import java.sql.Timestamp;

import me.timothy.jreddit.info.Link;

/**
 * Describes a link that was found while scanning a users history. This is a transient model
 * (ie. not expected to be saved for very long)
 * 
 * @author Timothy
 */
public class RedFlagUserHistoryLink {
	/** The id of the row */
	public int id;
	/** The id of the report this link was found while generating */
	public int reportId;
	/** The user who made this link */
	public int userId;
	/** The fullname of the link */
	public String fullname;
	/** The title of the link */
	public String title;
	/** The url the link points to if it points to one */
	public String url;
	/** The selftext for the link, if it has one */
	public String selfText;
	/** A permalink to the link */
	public String permalink;
	/** The subreddit this link was posted in */
	public String subreddit;
	/** When this link was posted */
	public Timestamp createdAt;
	/** Null if this is not a selftext / the selftext was never edited, otherwise the time of the last edit */
	public Timestamp editedAt;
	
	/**
	 * Create a new link history
	 * @param id the id in the database for this entry, or -1 if not in the database yet
	 * @param reportId the id of the report that generated this info
	 * @param userId the id of the user that posted this link
	 * @param fullname the fullname of the link
	 * @param title the title of the link
	 * @param url the url this link points to or null if this is self text
	 * @param selfText the text that this link has if it is selftext, null otherwise
	 * @param permalink a permalink to this submission
	 * @param subreddit the subreddit this was posted in
	 * @param createdAt when this link was posted
	 * @param editedAt null if this is not self text or has not been edited, otherwise the timestamp when this was last editted
	 */
	public RedFlagUserHistoryLink(int id, int reportId, int userId, String fullname, String title, String url,
			String selfText, String permalink, String subreddit, Timestamp createdAt, Timestamp editedAt) {
		super();
		this.id = id;
		this.reportId = reportId;
		this.userId = userId;
		this.fullname = fullname;
		this.title = title;
		this.url = url;
		this.selfText = selfText;
		this.permalink = permalink;
		this.subreddit = subreddit;
		this.createdAt = createdAt;
		this.editedAt = editedAt;
	}
	
	/**
	 * Create a new user history link that is based on the given link
	 * 
	 * @param link the link that was found
	 * @param reportId the id of the report this belongs to
	 * @param userId the user who posted this lin
	 */
	public RedFlagUserHistoryLink(Link link, int reportId, int userId) {
		this(-1, reportId, userId, link.fullname(), link.title(), link.url(), link.selftext(), 
				link.permalink(), link.subreddit(), new Timestamp((long)(link.createdUTC()*1000)), 
				link.edited() ? new Timestamp(link.timeEdited() * 1000) : null);
	}

	/**
	 * Checks if this is potentially valid without knowledge of the database
	 * @return if this is maybe valid
	 */
	public boolean isValid() {
		return (reportId > 0 && userId > 0 && fullname != null && title != null && permalink != null && createdAt != null && 
				(url != null || selfText != null) && ((url != null) != (selfText != null)) && subreddit != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((editedAt == null) ? 0 : editedAt.hashCode());
		result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
		result = prime * result + id;
		result = prime * result + ((permalink == null) ? 0 : permalink.hashCode());
		result = prime * result + reportId;
		result = prime * result + ((selfText == null) ? 0 : selfText.hashCode());
		result = prime * result + ((subreddit == null) ? 0 : subreddit.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		RedFlagUserHistoryLink other = (RedFlagUserHistoryLink) obj;
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
		if (reportId != other.reportId)
			return false;
		if (selfText == null) {
			if (other.selfText != null)
				return false;
		} else if (!selfText.equals(other.selfText))
			return false;
		if (subreddit == null) {
			if (other.subreddit != null)
				return false;
		} else if (!subreddit.equals(other.subreddit))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}
}
