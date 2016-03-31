package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.ResponseHistory;

/**
 * Describes a response history mapping
 * 
 * @author Timothy
 */
public interface ResponseHistoryMapping extends ObjectMapping<ResponseHistory> {
	/**
	 * Fetches the response history for the specified response 
	 * @param responseId the response to get history of
	 * @return the list of changes / history of the response
	 */
	public List<ResponseHistory> fetchForResponse(int responseId);
}
