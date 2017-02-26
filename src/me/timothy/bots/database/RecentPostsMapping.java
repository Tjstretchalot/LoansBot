package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.RecentPost;

/**
 * Describes a mapping for recent posts.
 * 
 * @author Timothy
 */
public interface RecentPostsMapping extends ObjectMapping<RecentPost> {
	/**
	 * Fetch all the recent posts by the specified username
	 * 
	 * @param username the username
	 * @return recent posts by username
	 */
	public List<RecentPost> fetchByUsername(String username);
	
	/**
	 * Removes entries older than a week from the database
	 */
	public void deleteOldEntries();
}
