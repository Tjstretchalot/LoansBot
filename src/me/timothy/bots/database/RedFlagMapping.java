package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.RedFlag;

/**
 * Describes a mapping for RedFlag s
 * 
 * @author Timothy
 */
public interface RedFlagMapping extends ObjectMapping<RedFlag> {
	/**
	 * Get all the red flags for the given report.
	 * @param reportId the id of the report
	 * @return the associated red flags.
	 */
	public List<RedFlag> fetchByReportID(int reportId);
	
	/**
	 * Get the red flags for the given report that match the given characteristics
	 * 
	 * @param reportId the report to check
	 * @param type the type of flag
	 * @param iden the identifier for the flag
	 * @return the flag that matches the search or null
	 */
	public RedFlag fetchByReportAndTypeAndIden(int reportId, String type, String iden);
}
