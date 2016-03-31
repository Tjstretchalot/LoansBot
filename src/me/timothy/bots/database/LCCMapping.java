package me.timothy.bots.database;

import me.timothy.bots.models.LendersCampContributor;

/**
 * A mapping for lenders camp contributors
 * 
 * @author Timothy
 */
public interface LCCMapping extends ObjectMapping<LendersCampContributor> {
	/**
	 * Checks if the specified user id can be found in the lenders camp contributor
	 * mapping
	 * 
	 * @param userId the user id to search for
	 * @return if the user is in the list
	 */
	public boolean contains(int userId);
}
