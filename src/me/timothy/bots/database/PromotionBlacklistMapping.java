package me.timothy.bots.database;

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
	public PromotionBlacklist fetchById(int userId);
	
	/**
	 * Fetches all entries, including deleted entries, for the given user id
	 * 
	 * @param userId the user id
	 * @return all related entries
	 */
	public List<PromotionBlacklist> fetchAllById(int userId);
	
	/**
	 * Removes the entry corresponding with the given user id. Note that this
	 * is just a shorthand for setting the removed at timestamp to now.
	 * 
	 * @param userId the user id to remove
	 */
	public void remove(int userId);
}
