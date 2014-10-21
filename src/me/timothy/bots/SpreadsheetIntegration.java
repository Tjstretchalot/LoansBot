package me.timothy.bots;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
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
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles google spreadsheet integration
 * 
 * @author Timothy
 */
public class SpreadsheetIntegration {
	@SuppressWarnings("unused")
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

	private static final String CLIENTLOGIN_URL = "https://www.google.com/accounts/ClientLogin";
	private static final String WORKSHEET_URL = "https://spreadsheets.google.com/feeds/list/1zG0N75D300IcOvwFt7dGIYFJqglJSvgPwjRamsqgOT0/ovovbg5/private/full";

	private LoansFileConfiguration config;

	private Authenticator gmailAuth;
	private Session imapsSession;
	private Properties propsSMTP;
	private Session smtpSession;
	private Store store;
	private Logger logger;

	/**
	 * Auth returned from ClientLogin
	 */
	private String auth;

	public SpreadsheetIntegration(LoansFileConfiguration cfg) {
		logger = LogManager.getLogger();
		config = cfg;

		init();
	}

	private void init() {
		Boolean res = new Retryable<Boolean>("Login to Google") {

			@Override
			protected Boolean runImpl() throws Exception {
				String query = "accountType=GOOGLE&" + "Email=" + URLEncoder.encode(config.getGoogleInfo().getProperty("username"), "UTF-8") + "&"
						+ "Passwd=" + URLEncoder.encode(config.getGoogleInfo().getProperty("password"), "UTF-8") + "&" + "service=wise&"
						+ "source=TimothyMoore-LoansBot-1.00.00";
				URL loginUrl = new URL(CLIENTLOGIN_URL);

				HttpsURLConnection con = (HttpsURLConnection) loginUrl.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				con.setRequestProperty("Content-Length", Integer.toString(query.length()));
				con.setRequestProperty("User-Agent", "Timothy Moore LoansBot application");
				con.setDoOutput(true);
				con.setDoInput(true);

				DataOutputStream output = new DataOutputStream(con.getOutputStream());
				output.writeBytes(query);
				output.close();

				Properties props = new Properties();
				InputStream input = con.getInputStream();
				props.load(input);
				input.close();

				auth = props.getProperty("Auth");

				if (con.getResponseCode() != 200) {
					logger.error("Non-200 response code from Google: " + con.getResponseCode());
					System.exit(1);
				}
				gmailAuth = new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(config.getGoogleInfo().getProperty("username"), config.getGoogleInfo().getProperty("password"));
					}
				};

				propsSMTP = new Properties();
				propsSMTP.put("mail.smtp.auth", "true");
				propsSMTP.put("mail.smtp.starttls.enable", "true");
				propsSMTP.put("mail.smtp.host", "smtp.gmail.com");
				propsSMTP.put("mail.smtp.port", "587");
				smtpSession = Session.getDefaultInstance(propsSMTP, gmailAuth);

				Properties propsImaps = new Properties();
				propsImaps.setProperty("mail.store.protocol", "imaps");
				propsImaps.putAll(propsSMTP);

				imapsSession = Session.getInstance(propsImaps, gmailAuth);

				try {
					store = imapsSession.getStore();
					store.connect("imap.gmail.com", null, null);
				} catch (NoSuchProviderException e) {
					logger.error(e);
					System.exit(1);
				} catch (MessagingException e) {
					logger.error(e);
					System.exit(1);
				}

				return true;
			}

		}.run();

		if (res == null)
			throw new RuntimeException("Failed to authenticate with Google");

	}

	/**
	 * Gets all pending applicants and removes them from the spreadsheet. <br>
	 * <br>
	 * Applicants order in the list is important for deleting them
	 * 
	 * @return pending applicants
	 */
	public List<Applicant> getPendingApplicants() {
		return new Retryable<List<Applicant>>("Get Pending Applicants") {

			@Override
			protected List<Applicant> runImpl() throws Exception {
				List<Applicant> result = new ArrayList<>();
				
				URL feed = new URL(WORKSHEET_URL);
				HttpsURLConnection con = (HttpsURLConnection) feed.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Authorization", "GoogleLogin auth=" + auth);
				con.setRequestProperty("User-Agent", "Timothy Moore LoansBot application");
				
				InputStream input = con.getInputStream();
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(input);
				input.close();
				
				doc.getDocumentElement().normalize();
				
				NodeList nList = doc.getElementsByTagName("entry");
				
				for(int i = 0; i < nList.getLength(); i++) {
					Node node = nList.item(i);
					
					if(node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						String timestamp = element.getElementsByTagName("title").item(0).getTextContent();
						String username = element.getElementsByTagName("gsx:" + USERNAME).item(0).getTextContent();
						String email = element.getElementsByTagName("gsx:" + EMAIL).item(0).getTextContent();
						String firstName = element.getElementsByTagName("gsx:" + FIRST_NAME).item(0).getTextContent();
						String lastName = element.getElementsByTagName("gsx:" + LAST_NAME).item(0).getTextContent();
						String streetAddr = element.getElementsByTagName("gsx:" + STREET_ADDRESS).item(0).getTextContent();
						String city = element.getElementsByTagName("gsx:" + CITY).item(0).getTextContent();
						String zip = element.getElementsByTagName("gsx:" + ZIP_CODE).item(0).getTextContent();
						String state = element.getElementsByTagName("gsx:" + STATE).item(0).getTextContent();
						String country = element.getElementsByTagName("gsx:" + COUNTRY).item(0).getTextContent();
						String paymentMethod = element.getElementsByTagName("gsx:" + PAYMENT_METHOD).item(0).getTextContent();
						String mainMethodOfUse = element.getElementsByTagName("gsx:" + MAIN_METHOD_OF_USE).item(0).getTextContent();
						
						logger.info("adding applicant");
						result.add(new Applicant(timestamp, username, email, firstName, lastName, streetAddr, city, zip, state, country, paymentMethod, mainMethodOfUse));
					}
				}
				return result;
			}
			
		}.run();
	}

	/**
	 * Removes the top applicant
	 */
	public void removeTopApplicant() {
	}

	/**
	 * Sends the specified email
	 * 
	 * @param to
	 *            who to send the email to
	 * @param firstName
	 *            first name of that person
	 * @param title
	 *            title of email
	 * @param message
	 *            email message
	 * @return success
	 */
	public boolean sendEmail(final Address to, final String title, final String message) {
		return new Retryable<Boolean>("Send Email") {

			@Override
			protected Boolean runImpl() throws Exception {
				
				try {
					Message msg = new MimeMessage(smtpSession);
					msg.setFrom(new InternetAddress(config.getGoogleInfo().getProperty("username"), "LoansBot"));
					msg.addRecipient(Message.RecipientType.TO,
							to);
					msg.setSubject(title);
					msg.setText(message);
					Transport.send(msg);
				}catch(SendFailedException sfe) {
					if(sfe.getMessage().equals("Invalid Addresses"))
						return false;
					sfe.printStackTrace();
					return null;
				}
				return true;
			}

		}.run();
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
