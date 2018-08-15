package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.RedFlagReport;

/**
 * Describes a mapping for RedFlagReport s
 * 
 * @author Timothy
 */
public interface RedFlagReportMapping extends ObjectMapping<RedFlagReport> {
	/**
	 * Fetch the report with the given id
	 * 
	 * @param id the id of the red flag report to fetch
	 * @return the corresponding report
	 */
	public RedFlagReport fetchByID(int id);
	
	/**
	 * Fetch the reports for the given username.
	 * 
	 * @param usernameId the id of the username 
	 * @return the corresponding reports generated
	 */
	public List<RedFlagReport> fetchByUsernameID(int usernameId);
}
