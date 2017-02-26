package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Enough information to determine if a user is reposting too often. Describes
 * one post to a subreddit, and is expected to prune >1 week old entries
 * 
 * @author Timothy
 */
public class RecentPost {
	public int id;
	public String author;
	public String subreddit;
	
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public RecentPost(int id, String author, String subreddit, Timestamp createdAt, Timestamp updatedAt)
	{
		this.id = id;
		this.author = author;
		this.subreddit = subreddit;
		
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public boolean isValid()
	{
		return author != null && subreddit != null && createdAt != null && updatedAt != null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((subreddit == null) ? 0 : subreddit.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
		RecentPost other = (RecentPost) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (subreddit == null) {
			if (other.subreddit != null)
				return false;
		} else if (!subreddit.equals(other.subreddit))
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return "RecentPost [id=" + id + ", author=" + author + ", subreddit=" + subreddit + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
}
