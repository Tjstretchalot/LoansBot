package me.timothy.bots;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		
		addProperties("database", true, "url", "username", "password", "use_ssl");
		addProperties("currencylayer", true, "access_code");
		addProperties("rechecks", true, "silent_mode");
		addProperties("red_flags", true, "suppress");
		addProperties("lenders_camp", true, "num_completed_as_lender", "num_started_as_lender", "ms_since_oldest_paid");
		
		// Verify we won't get an exception later
		try {
			Integer.parseInt(getProperty("lenders_camp.num_completed_as_lender"));
			Integer.parseInt(getProperty("lenders_camp.num_started_as_lender"));
			Integer.parseInt(getProperty("lenders_camp.ms_since_oldest_paid"));
		}catch(NumberFormatException e) {
			Logger logger = LogManager.getLogger();
			logger.fatal("Lenders camp properties failed to be converted to ints");
			logger.throwing(e);
			throw e;
		}
	}
}
