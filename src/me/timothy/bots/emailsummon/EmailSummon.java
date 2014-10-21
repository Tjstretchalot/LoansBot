package me.timothy.bots.emailsummon;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;

/**
 * Describes a Summon that is for emails as well
 * @author Timothy
 *
 */
public interface EmailSummon {
	
	/**
	 * Checks if the following message is summoned
	 * in this way, or at least necessitates a response
	 * by this summon.
	 * 
	 * @param subject the subject 
	 * @param message the message
	 * @return if this summon is applicable to the message
	 */
	public boolean isSummonedBy(String subject, String message);
	
	/**
	 * Parses the specified message, which has
	 * already returned true from isSummonedBy
	 * 
	 * @param subject the subject
	 * @param message the message to parse
	 */
	public void parse(String subject, String message);
	
	/**
	 * Takes whatever the internal state of this summon
	 * has from the last parse, and applies those changes
	 * @return
	 */
	public String applyDatabaseChanges(FileConfiguration config, Database database) throws IllegalStateException;
}
