package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.ShareCode;

/**
 * Describes a mapping for share codes
 * 
 * @author Timothy
 */
public interface ShareCodeMapping extends ObjectMapping<ShareCode> {
	/**
	 * Fetches the list of sharecodes for the specified user
	 * @param userId the user to get sharecodes of
	 * @return the users share codes or an empty list
	 */
	public List<ShareCode> fetchForUser(int userId);
	
	/**
	 * Deletes the specified share code from the mapping
	 * @param shareCode the share code to delete
	 */
	public void delete(ShareCode shareCode);
}
