package me.timothy.bots;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles google spreadsheet integration
 * 
 * @author Timothy
 */
public class SpreadsheetIntegration {
	private static final String TIMESTAMP = "timestamp";
	private static final String USERNAME = "redditusername";
	private static final String EMAIL = "emailaddress";
	private static final String FIRST_NAME = "firstname";
	private static final String LAST_NAME = "lastname";
	private static final String STREET_ADDRESS = "streetaddress";
	private static final String CITY = "citytown";
	private static final String ZIP_CODE = "zipcode";
	private static final String STATE = "state";
	private static final String COUNTRY = "country";
	private static final String PAYMENT_METHOD = "paymentmethod";
	private static final String MAIN_METHOD_OF_USE = "mainmethodofuse";

	private LoansFileConfiguration config;

	private final URL SPREADSHEET_FEED_URL;

	@SuppressWarnings("unused")
	private Logger logger;

	private Properties propsSMTP;

	private boolean initSuccessful;

	public SpreadsheetIntegration(LoansFileConfiguration cfg) {
		logger = LogManager.getLogger();
		config = cfg;

		try {
			SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		init();
	}


	private void init() {
		initSuccessful = true;
	}


	/**
	 * Gets all pending applicants and removes them from the spreadsheet.
	 * <br><br>
	 * Applicants order in the list is important for deleting them
	 * @return pending applicants
	 */
	public List<Applicant> getPendingApplicants() {
		return null;
	}

	/**
	 * Removes the top applicant
	 */
	public void removeTopApplicant() {
	}


	/**
	 * Sends the specified email
	 * 
	 * @param to who to send the email to
	 * @param firstName first name of that person
	 * @param title title of email
	 * @param message email message
	 * @return success
	 */
	public boolean sendEmail(final String to, final String title, final String message) {
		return true;
	}
}
