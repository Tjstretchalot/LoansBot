package me.timothy.bots.database;

import me.timothy.bots.models.DelayedVettingRequest;

/**
 * Maps a request to try vetting a user again later to/from the database
 * 
 * @author Timothy
 * @see me.timothy.bots.models.DelayedVettingRequest
 */
public interface DelayedVettingRequestMapping extends ObjectMapping<DelayedVettingRequest> {
	/**
	 * Fetches the incomplete vetting request associated with the given user
	 * 
	 * @param userId the user
	 * @return the incomplete delayed vetting request for that user or null if there is not one
	 */
	public DelayedVettingRequest fetchByUserId(int userId);
}
