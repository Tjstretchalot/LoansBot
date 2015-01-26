package me.timothy.bots.responses;

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
	 * Turns this object into a formatted one via
	 * a list of objects
	 * 
	 * @param myName the name of this object
	 * @param info the list of all objects
	 * @return the formatted version of this
	 */
	public String toFormattedString(String myName, ResponseInfo info);
}
