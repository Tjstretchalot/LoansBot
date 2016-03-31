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
	 * Fetches all the warnings for the specified user id
	 * 
	 * @param userId the user id to fetch warnings of
	 * @return the warnings of that user, or an empty list
	 */
	public List<Warning> fetchByUserId(int userId);
}
