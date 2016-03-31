package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.CreationInfo;

/**
 * Describes a mapping for a creation info
 * 
 * @author Timothy
 */
public interface CreationInfoMapping extends ObjectMapping<CreationInfo> {
	/**
	 * Attempts to fetch the creation info using its id.
	 * @param id the id of the creation info
	 * @return either the CreationInfo from the mapping or null
	 */
	public CreationInfo fetchById(int id);
	
	/**
	 * Fetches creation infos for <i>any</i> of the specified loan ids
	 * 
	 * @param loanIds the loan ids to fetch
	 * @return creation infos with a loan id included in the list of loan ids, or an empty list
	 */
	public List<CreationInfo> fetchManyByLoanIds(int... loanIds);
	/**
	 * Attempts to fetch the creation info for the specified loan
	 * @param loanId the loan
	 * @return its creation info or null
	 */
	public CreationInfo fetchByLoanId(int loanId);
}
