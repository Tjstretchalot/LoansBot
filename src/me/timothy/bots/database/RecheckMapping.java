package me.timothy.bots.database;

import me.timothy.bots.models.Recheck;

/**
 * Describes a recheck mapping
 * 
 * @author Timothy
 */
public interface RecheckMapping extends ObjectMapping<Recheck> {
	/**
	 * Deletes the specified recheck from the mapping
	 * @param recheck the recheck to delete
	 */
	public void delete(Recheck recheck);
}
