package me.timothy.bots.database;

import me.timothy.bots.models.FailedLoginAttempt;

/**
 * Maps failed login attempts to/from the database
 * 
 * @author Timothy
 */
public interface FailedLoginAttemptMapping extends ObjectMapping<FailedLoginAttempt> {
	/**
	 * Prunes old entries from the database
	 */
	public void prune();
}
