package me.timothy.bots.database;

import java.sql.Timestamp;
import java.util.List;

import me.timothy.bots.models.RedFlagUserHistoryComment;

/**
 * Maps red flag user history comments
 * 
 * @author Timothy
 */
public interface RedFlagUserHistoryCommentMapping extends ObjectMapping<RedFlagUserHistoryComment> {
	/**
	 * Fetch the comment with the given id.
	 * 
	 * @param id the id in the database
	 * @return the corresponding comment
	 */
	public RedFlagUserHistoryComment fetchByID(int id);
	
	/**
	 * Fetch any comments for the given report that occurred at the given timestamp
	 * @param reportId the report
	 * @param timestamp the timestamp
	 * @return comments for that report at that time
	 */
	public List<RedFlagUserHistoryComment> fetchAtTimestamp(int reportId, Timestamp timestamp);
	
	/**
	 * Fetch the next comment that is strictly after the given timestamp
	 * @param reportId the report you are considering
	 * @param timestamp the timestamp
	 * @return the next comment after that timestamp
	 */
	public RedFlagUserHistoryComment fetchNextAfter(int reportId, Timestamp timestamp);
	
	/**
	 * Delete all the saved user history comments that were generated 
	 * for the specified report id.
	 * 
	 * @param reportId the id of the report to delete
	 */
	public void deleteByReportID(int reportId);
}
