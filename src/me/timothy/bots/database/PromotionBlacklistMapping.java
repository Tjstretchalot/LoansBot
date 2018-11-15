package me.timothy.bots.database;

import me.timothy.bots.models.PromotionBlacklist;

/**
 * Maps PromotionBlacklist users
 * 
 * @author Timothy
 */
public interface PromotionBlacklistMapping extends ObjectMapping<PromotionBlacklist> {
	/** 
	 * Determines if the given person id is on the promotion blacklist.
	 * 
	 * @param personId the id of the person
	 * @return if they are on the blacklist
	 */
	public boolean contains(int personId);
}
