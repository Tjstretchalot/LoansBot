package me.timothy.bots.database;

import me.timothy.bots.models.BannedUser;

public interface BannedUserMapping extends ObjectMapping<BannedUser> {
	/**
	 * Determines if this mapping contains the specified user id.
	 * 
	 * @param userID the user id to check
	 * @return if this mapping contains it
	 */
	public boolean containsUserID(int userID);
	
	/**
	 * Removes the banned user in this mapping that corresponds
	 * with the specifid user id.
	 * 
	 * @param userID the user id to remove
	 */
	public void removeByUserID(int userID);
}
