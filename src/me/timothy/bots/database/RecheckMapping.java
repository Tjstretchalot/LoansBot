package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.Recheck;

/**
 * Describes a recheck mapping
 * 
 * @author Timothy
 */
public interface RecheckMapping extends ObjectMapping<Recheck> {
	/**
	 * Fetches all rechecks in the mapping
	 * @return all rechecks
	 */
	public List<Recheck> fetchAll();
	
	/**
	 * Deletes the specified recheck from the mapping
	 * @param recheck the recheck to delete
	 */
	public void delete(Recheck recheck);
}
