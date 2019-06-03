package me.timothy.bots.database;

import me.timothy.bots.models.ResponseOptOut;

/**
 * Handles managing the response opt out list
 * 
 * @author Timothy
 */
public interface ResponseOptOutMapping extends ObjectMapping<ResponseOptOut> {
	/**
	 * Determines if the specified user id is in this mapping
	 * @param userId the user id you are interested in
	 * @return true if the user is in the mapping, false otherwise
	 */
	public boolean contains(int userId);
}
