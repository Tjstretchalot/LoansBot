package me.timothy.bots.redflags;

import java.util.List;

import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.Username;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;

/**
 * Describes something capable of detecting red flags. These are given lots
 * of calls because many of them will need tables of their own. However,
 * they should delete anything they make after the finish callback and use
 * the red flags themselves for historic purposes
 * 
 * @author Timothy
 */
public interface IRedFlagDetector {
	/**
	 * This is called when we're starting a scan for the given username
	 * for the first time.
	 * 
	 * @param username the username we are scanning
	 */
	public void start(Username username);
	
	/**
	 * Called when we are about to resume looking at a usernames history.
	 * 
	 * @param username the username who we've previously paused
	 */
	public void resume(Username username);
	
	/**
	 * Parse the given comment and return any red flags it raised for you. The 
	 * resulting red flags should not have been saved to the database yet, and
	 * will be deduplicated prior to saving.
	 * 
	 * @param comment the comment
	 * @return the red flags we should save due to the comment
	 */
	public List<RedFlag> parseComment(Comment comment);
	
	/**
	 * Parse the given link and return any red flags it raised for you. The 
	 * resulting red flags should not have been saved to the database yet, and 
	 * will be deduplicated prior to saving.
	 * 
	 * @param link the link
	 * @return the red flags we should save due to the link
	 */
	public List<RedFlag> parseLink(Link link);
	
	/**
	 * Pause parsing for the current username.
	 */
	public void pause();
	
	/**
	 * Called when we've completed parsing the username passed to the last start
	 * or resume call. These red flags are treated just like the parse functions
	 * 
	 * @return any final red flags we should save
	 */
	public List<RedFlag> finish();
}
