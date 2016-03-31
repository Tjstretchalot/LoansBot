package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.Username;

/**
 * Describes a mapping for usernames
 * 
 * @author Timothy
 */
public interface UsernameMapping extends ObjectMapping<Username> {
	/**
	 * Fetches the username with the specified id
	 * 
	 * @param usernameId the id of the username
	 * @return the username with that id or null
	 */
	public Username fetchById(int usernameId);
	
	/**
	 * Fetches all the usernames the specified user id has
	 * 
	 * @param userId the user id 
	 * @return all usernames the user id is known by, or an empty list
	 */
	public List<Username> fetchByUserId(int userId);
	
	/**
	 * Fetches the username with the specified username as its username (?!)
	 * 
	 * @param username the username to search for
	 * @return the username model it matches, or null
	 */
	public Username fetchByUsername(String username);
}