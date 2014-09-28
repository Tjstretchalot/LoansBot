package me.timothy.bots.emailsummon;

import javax.mail.Message;

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
	 * @param msg the message to look into
	 * @return if this summon is applicable to the message
	 */
	public boolean isSummonedBy(Message msg);
	
	/**
	 * Parses the specified message, which has
	 * already returned true from isSummonedBy
	 * 
	 * @param msg the message to parse
	 */
	public void parse(Message msg);
	
	/**
	 * Takes whatever the internal state of this summon
	 * has from the last parse, and applies those changes
	 * @return
	 */
	public String applyDatabaseChanges(FileConfiguration config, Database database) throws IllegalStateException;
}
