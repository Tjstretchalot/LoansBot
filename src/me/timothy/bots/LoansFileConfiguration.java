package me.timothy.bots;

import java.io.IOException;

/**
 * Loads lots and lots of various strings/properties from there appropriate
 * files and places them in the appropriate hash. The keys match the file names
 * (without the extension)
 * 
 * @author Timothy
 */
public class LoansFileConfiguration extends FileConfiguration {
	/**
	 * Loads all the necessary configuration
	 * 
	 * @throws IOException
	 *             if an io-exception occurs (or a file is not found)
	 * @throws NullPointerException
	 *             if a required key is missing
	 */
	public void load() throws IOException, NullPointerException {
		super.load();
		
		addProperties("database", true, "url", "username", "password");
		addProperties("currencylayer", true, "access_code");
	}
}
