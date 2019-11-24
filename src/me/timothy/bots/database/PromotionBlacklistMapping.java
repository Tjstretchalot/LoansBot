package me.timothy.bots.database;

import java.sql.Timestamp;
import java.util.List;

import me.timothy.bots.models.PromotionBlacklist;

/**
 * Maps PromotionBlacklist users
 * 
 * @author Timothy
 */
public interface PromotionBlacklistMapping extends ObjectMapping<PromotionBlacklist> {
	/** 
	 * Determines if the given person id is on the promotion blacklist, ignoring
	 * entries where deleted at is not null
	 * 
	 * @param userId the id of the user
	 * @return if they are on the blacklist
	 */
	public boolean contains(int userId);
	
	/**
	 * Fetches the person on the promotion blacklist with the given user id,
	 * ignoring rows where deleted at is not null.
	 * 
	 * @param userId the user id of the person 
	 * @return the corresponding row in the database, null if none
	 */
	public PromotionBlacklist fetchByUserId(int userId);
	
	/**
	 * Fetches all entries, including deleted entries, for the given user id
	 * 
	 * @param userId the user id
	 * @return all related entries
	 */
	public List<PromotionBlacklist> fetchAllById(int userId);
	
	/**
	 * Fetches up to the given limit number of promotion blacklist items which are not 
	 * deleted and were added after the given time. This is ordered by oldest-to-newest
	 * added_at, meaning this is suitable for pagination.
	 * 
	 * @param time the earliest (excluded) time for loans to occur, or null to be from the
	 * beginning of time
	 * @param limit the maximum number of results
	 * @return up to limit promotion blacklist rows
	 */
	public List<PromotionBlacklist> fetchUndeletedAndAddedAfter(Timestamp time, int limit);
	
	/**
	 * Removes the entry corresponding with the given user id. Note that this
	 * is just a shorthand for setting the removed at timestamp to now.
	 * 
	 * @param userId the user id to remove
	 */
	public void remove(int userId);
}
