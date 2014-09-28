package me.timothy.bots;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	private LoansFileConfiguration config;
	
	private final URL SPREADSHEET_FEED_URL;

	private Logger logger;
	
	private SpreadsheetService service;

	private SpreadsheetEntry mSpreadsheet;
	private WorksheetEntry wEntry;
	
	private Authenticator gmailAuth;
	private Session imapsSession;
	private Session smtpSession;
	
	private Store store;

	public SpreadsheetIntegration(LoansFileConfiguration cfg) {
		logger = LogManager.getLogger();
		config = cfg;
		
		// SPREADSHEET 
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
			for(SpreadsheetEntry se : entries) {
				if(se.getTitle().getPlainText().equals("/r/Borrow Application (Responses)")) {
					mSpreadsheet = se;
					break;
				}
			}

			WorksheetFeed wFeed = service.getFeed(mSpreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
			List<WorksheetEntry> wEntries = wFeed.getEntries();

			wEntry = wEntries.get(0);
		} catch (IOException | ServiceException e) {
			throw new RuntimeException(e);
		}
		
		// EMAIL
		gmailAuth = new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(config.getGoogleInfo().getProperty("username"), config.getGoogleInfo().getProperty("password"));
			}
		};
		
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		
		imapsSession = Session.getInstance(props, gmailAuth);

		Properties propsSMTP = new Properties();
		propsSMTP.put("mail.smtp.auth", "true");
		propsSMTP.put("mail.smtp.starttls.enable", "true");
		propsSMTP.put("mail.smtp.host", "smtp.gmail.com");
		propsSMTP.put("mail.smtp.port", "587");

		smtpSession = Session.getDefaultInstance(propsSMTP, gmailAuth);
		try {
			store = imapsSession.getStore();
			store.connect("imap.gmail.com", null, null);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (MessagingException e) {
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


	/**
	 * Sends the specified email
	 * 
	 * @param to who to send the email to
	 * @param firstName first name of that person
	 * @param title title of email
	 * @param message email message
	 * @returnsuccess
	 */
	public boolean sendEmail(final Address to, final String title, final String message) {
		try {
			Message msg = new MimeMessage(smtpSession);
			msg.setFrom(new InternetAddress(config.getGoogleInfo().getProperty("username"), "LoansBot"));
			msg.addRecipient(Message.RecipientType.TO,
					to);
			msg.setSubject(title);
			msg.setText(message);
			Transport.send(msg);
		} catch(SendFailedException invAddress) {
			if(invAddress.getMessage().equals("Invalid Addresses"))
				return false;
			throw new RuntimeException(invAddress);
		}catch (MessagingException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return true;
	}
	
	/**
	 * Gets the email inbox of the loans bot. It is already
	 * opened in READ-ONLY mode
	 * @return the inbox
	 * @throws MessagingException if an exception occurs
	 */
	public Folder getInbox() throws MessagingException {
		Folder inb = store.getFolder("INBOX");
		inb.open(Folder.READ_ONLY);
		return inb;
	}
}
