package me.timothy.bots.database;

import me.timothy.bots.models.RedFlagForSubreddit;

/**
 * Describes a mapping for red flags associated with subreddits
 * 
 * @author Timothy
 */
public interface RedFlagForSubredditMapping extends ObjectMapping<RedFlagForSubreddit> {
	/**
	 * Fetch the red flag for the given subreddit if there is one
	 * 
	 * @param subreddit the subreddit
	 * @return the associated red flag
	 */
	public RedFlagForSubreddit fetchBySubreddit(String subreddit);
}
