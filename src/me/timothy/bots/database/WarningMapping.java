package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.Warning;

/**
 * Describes a mapping for warnings
 * 
 * @author Timothy
 */
public interface WarningMapping extends ObjectMapping<Warning> {
	/**
	 * Fetches all the warnings where the specified user id
	 * is the warned user id.
	 * 
	 * @param userId the user id to fetch warnings of
	 * @return the warnings of that user, or an empty list
	 */
	public List<Warning> fetchByWarnedUserId(int userId);
}
