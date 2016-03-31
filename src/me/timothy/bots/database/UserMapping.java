package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.User;

/**
 * Describes a user mapping
 * 
 * @author Timothy
 */
public interface UserMapping extends ObjectMapping<User> {
	/**
	 * Fetches the user by the specified id 
	 * @param id the id of the user
	 * @return the user or null
	 */
	public User fetchById(int id);
	
	/**
	 * Either fetches the user with the specified username, or 
	 * if no such user exists, create one.
	 * @param username the username of the user
	 * @return the user
	 */
	public User fetchOrCreateByName(String username);
	
	/**
	 * Fetches the highest user id
	 * @return the highest user id
	 */
	public int fetchMaxUserId();
	
	/**
	 * Fetches the list of users that are <i>unclaimed</i>, have a <i>claim code</i>,
	 * and do not have a <i>claim link sent at</i> date.
	 * 
	 * @return The list of users to send a code to
	 */
	public List<User> fetchUsersToSendCode();
}
