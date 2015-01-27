package me.timothy.bots.responses;

import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;

/**
 * Ever response object must implement this interface.
 * Basically, this turns a standard object into one
 * with pretty reddit formatting
 * 
 * @author Timothy
 *
 */
public interface FormattableObject {

	/**
	 * Formats an object using the data given
	 * @param info the info this object belongs to
	 * @param myName the name of the key of this object
	 * @param config the current config
	 * @param db the db 
	 * @return the formatted string
	 */
	public String toFormattedString(ResponseInfo info, String myName, FileConfiguration config, LoansDatabase db);
}
