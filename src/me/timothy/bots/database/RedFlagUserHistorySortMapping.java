package me.timothy.bots.database;

import me.timothy.bots.models.RedFlagUserHistorySort;

/**
 * Maps the red flag user history sort to/from the database
 * 
 * @author Timothy
 */
public interface RedFlagUserHistorySortMapping extends ObjectMapping<RedFlagUserHistorySort> {
	/**
	 * Fetch the next item to process.
	 * 
	 * @param reportId the report you are processing
	 * @param previous the last sort value (or -1 if just starting)
	 * @return the next thing to process
	 */
	public RedFlagUserHistorySort fetchNext(int reportId, int previous);
	
	/**
	 * Produces and saves the sort from the given report.
	 * @param database the mapping database since this requires multi-table interaction
	 * @param reportId the report who has links/comments saved
	 */
	public void produceSort(MappingDatabase database, int reportId);
	
	/**
	 * Delete the sort corresponding to the given report
	 * @param reportId the report
	 */
	public void deleteByReport(int reportId);
}
