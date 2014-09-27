package me.timothy.bots;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

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

	private final URL SPREADSHEET_FEED_URL;

	private SpreadsheetService service;

	private SpreadsheetEntry mSpreadsheet;
	private WorksheetEntry wEntry;

	public SpreadsheetIntegration(LoansFileConfiguration config) {
		try {
			SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		service = new SpreadsheetService("/u/LoansBot Spreadsheet Integration");

		try {
			service.setUserCredentials(config.getGoogleInfo().getProperty("username"), config.getGoogleInfo().getProperty("password"));

			SpreadsheetFeed sFeed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
			List<SpreadsheetEntry> entries = sFeed.getEntries();

			mSpreadsheet = entries.get(0);

			WorksheetFeed wFeed = service.getFeed(mSpreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
			List<WorksheetEntry> wEntries = wFeed.getEntries();

			wEntry = wEntries.get(0);
		} catch (IOException | ServiceException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Gets all pending applicants and removes them from the spreadsheet.
	 * <br><br>
	 * Applicants order in the list is important for deleting them
	 * @return pending applicants
	 */
	public List<Applicant> getPendingApplicants() {
		List<Applicant> result = new ArrayList<>();

		ListFeed lFeed = new Retryable<ListFeed>("Get list-feed") {

			@Override
			protected ListFeed runImpl() throws Exception {
				return service.getFeed(wEntry.getListFeedUrl(), ListFeed.class);
			}
			
		}.run();

		List<ListEntry> rows = lFeed.getEntries();

		for(ListEntry row : rows) {
			CustomElementCollection cec = row.getCustomElements();
			result.add(new Applicant(cec.getValue(TIMESTAMP), 
					cec.getValue(USERNAME), 
					cec.getValue(EMAIL), 
					cec.getValue(FIRST_NAME), 
					cec.getValue(LAST_NAME), 
					cec.getValue(STREET_ADDRESS), 
					cec.getValue(CITY), 
					cec.getValue(ZIP_CODE), 
					cec.getValue(STATE),
					cec.getValue(COUNTRY), 
					cec.getValue(PAYMENT_METHOD),
					cec.getValue(MAIN_METHOD_OF_USE)
					));
		}
		return result;
	}

	/**
	 * Removes the top applicant
	 */
	public void removeTopApplicant() {
		new Retryable<Boolean>("Delete top row") {

			@Override
			protected Boolean runImpl() throws Exception {
				ListFeed lFeed = service.getFeed(wEntry.getListFeedUrl(), ListFeed.class);
				List<ListEntry> rows = lFeed.getEntries();
				rows.get(0).delete();
				return true;
			}
		}.run();
	}
}
