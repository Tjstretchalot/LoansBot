package me.timothy.bots.redflags;

import java.util.List;

import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.Username;

/**
 * Describes something capable of detecting red flags. These are guaranteed to do the entire
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
	 * Parse the given comment and return any red flags it raised for you. The 
	 * resulting red flags should not have been saved to the database yet, and
	 * will be deduplicated prior to saving.
	 * 
	 * @param comment the comment
	 * @return the red flags we should save due to the comment
	 */
	public List<RedFlag> parseComment(RedFlagUserHistoryComment comment);
	
	/**
	 * Parse the given link and return any red flags it raised for you. The 
	 * resulting red flags should not have been saved to the database yet, and 
	 * will be deduplicated prior to saving.
	 * 
	 * @param link the link
	 * @return the red flags we should save due to the link
	 */
	public List<RedFlag> parseLink(RedFlagUserHistoryLink link);
	
	/**
	 * Called when we've completed parsing the username passed to the last start
	 * or resume call. These red flags are treated just like the parse functions
	 * 
	 * @return any final red flags we should save
	 */
	public List<RedFlag> finish();
	
	/**
	 * This is called immediately after finish() and can return true to 
	 * get the same user reprocessed immediately.
	 * 
	 * @return true for a resweep, false otherwise
	 */
	public boolean requiresResweep();
}
