package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a subreddit which when we see that a user
 * has used we add a red flag.
 * 
 * @author Timothy
 */
public class RedFlagForSubreddit {
	public int id;
	public String subreddit;
	public String description;
	public Timestamp createdAt;

	/**
	 * @param id the database identifier or -1 if not in the database yet
	 * @param subreddit the subreddit 
	 * @param description the description markdown to post if we find activity in the subreddit
	 * @param createdAt when we added this to the database
	 */
	public RedFlagForSubreddit(int id, String subreddit, String description, Timestamp createdAt) {
		super();
		this.id = id;
		this.subreddit = subreddit;
		this.description = description;
		this.createdAt = createdAt;
	}

	/**
	 * Determine if this has all the necessary parts to save to the database
	 * @return if this is complete / valid for saving
	 */
	public boolean isValid() {
		return (subreddit != null && description != null && createdAt != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
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
		RedFlagForSubreddit other = (RedFlagForSubreddit) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (subreddit == null) {
			if (other.subreddit != null)
				return false;
		} else if (!subreddit.equals(other.subreddit))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RedFlagForSubreddit [id=" + id + ", subreddit=" + subreddit + ", description=" + description
				+ ", createdAt=" + createdAt + "]";
	}
}
