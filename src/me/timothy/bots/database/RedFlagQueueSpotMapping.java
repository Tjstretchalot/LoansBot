package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.RedFlagQueueSpot;

/**
 * Describes a mapping for RedFlagQueueSpot s
 * 
 * @author Timothy
 */
public interface RedFlagQueueSpotMapping extends ObjectMapping<RedFlagQueueSpot> {
	/**
	 * Fetch the queue spot with the given id.
	 * @param id the id of the queue spot
	 * @return the corresponding queue spot or null if not found
	 */
	public RedFlagQueueSpot fetchByID(int id);
	
	/**
	 * Fetch the oldest red flag queue spot that is not yet completed
	 * @return the oldest uncompleted queue spot
	 */
	public RedFlagQueueSpot fetchOldestUncompleted();
	
	/**
	 * Fetch the red flag queue spots with the given RedFlagReport id
	 * @param reportId the RedFlagReport id
	 * @param onlyUncompleted if true, only uncompleted queue spots are returned
	 * @return the queue spots for the corresponding report id
	 */
	public List<RedFlagQueueSpot> fetchByReportId(int reportId, boolean onlyUncompleted);
	
	/**
	 * Fetch the red flag queue spots corresponding to the given username id 
	 * @param usernameId the id of the username 
	 * @param onlyUncompleted if true, only uncompleted queue spots are returned.
	 * @return the queue spots for the given username id
	 */
	public List<RedFlagQueueSpot> fetchByUsername(int usernameId, boolean onlyUncompleted);
}
