package me.timothy.bots.database;

import java.util.List;

import me.timothy.bots.models.ResetPasswordRequest;

/**
 * Describes a mapping for reset password requests
 * 
 * @author Timothy
 */
public interface ResetPasswordRequestMapping extends ObjectMapping<ResetPasswordRequest> {
	/**
	 * Fetches all reset password requests that have not been sent out yet.
	 * 
	 * @return the list of reset password requests that have not been sent out yet, or an empty list
	 */
	public List<ResetPasswordRequest> fetchUnsent();
}
