package me.timothy.bots.database;

import me.timothy.bots.models.BorrowerReqPMOptOut;

/**
 * Maps the borrower request pm out opt objects to/from the database
 * 
 * @author Timothy
 */
public interface BorrowerReqPMOptOutMapping extends ObjectMapping<BorrowerReqPMOptOut> {
	/**
	 * Tests if the user with the given id has opted out of receiving
	 * a PM when a borrower with which they have an active loan makes
	 * a request thread.
	 * 
	 * @param userId the id of the user to check
	 * @return true if they have opted out, false if they have not
	 */
	public boolean contains(int userId);
}
