package me.timothy.bots;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads lots and lots of various strings/properties from there appropriate
 * files
 * 
 * @author Timothy
 */
public class LoansFileConfiguration extends FileConfiguration {
	private static final String DATABASE_INFO_FILE = "database.properties";

	private static final String SUCCESSFUL_LOAN = "successful_loan.txt";
	private static final String NO_LOANS_TO_REPAY = "no_loans_to_repay.txt";
	private static final String REPAYMENT = "repayment.txt";
	private static final String LOAN_FORMAT = "loan.txt";
	private static final String CHECK = "check.txt";
	private static final String CHECK_TRUNCATED = "check_truncated.txt";
	private static final String UNPAID = "unpaid.txt";
	private static final String CONFIRM = "confirm.txt";
	
	private static final String ACTION_TO_BANNED = "action_to_banned.txt";
	
	private Properties databaseInfo;
	private String successfulLoan;
	private String noLoansToRepay;
	private String repayment;
	private String loanFormat;
	private String check;
	private String checkTruncated;
	private String actionToBanned;
	private String unpaid;
	private String confirm;

	private Logger logger;

	public LoansFileConfiguration() {
		logger = LogManager.getLogger();
	}

	/**
	 * Loads all the necessary configuration
	 * 
	 * @throws IOException
	 *             if an io-exception occurs (or a file is not found)
	 * @throws NullPointerException
	 *             if a required key is missing
	 */
	public void load() throws IOException, NullPointerException {
		databaseInfo = loadProperties(Paths.get(DATABASE_INFO_FILE).toFile(), "url", "username", "password");
		successfulLoan = loadReplyString(Paths.get(SUCCESSFUL_LOAN).toFile());
		noLoansToRepay = loadReplyString(Paths.get(NO_LOANS_TO_REPAY).toFile());
		repayment = loadReplyString(Paths.get(REPAYMENT).toFile());
		loanFormat = loadReplyString(Paths.get(LOAN_FORMAT).toFile());
		setActionToBanned(loadReplyString(Paths.get(ACTION_TO_BANNED).toFile()));
		check = loadReplyString(Paths.get(CHECK).toFile());
		checkTruncated = loadReplyString(Paths.get(CHECK_TRUNCATED).toFile());
		unpaid = loadReplyString(Paths.get(UNPAID).toFile());
		confirm = loadReplyString(Paths.get(CONFIRM).toFile());
	}

	/**
	 * Loads properties from the specified file, as if by
	 * {@link java.util.Properties#load(java.io.Reader)}
	 * 
	 * @param file
	 *            the file to load from
	 * @param requiredKeys
	 *            the list of required keys
	 * @return the properties in the {@code file}
	 * @throws IOException
	 *             if an i/o exception occurs (like {@code file} not existing)
	 * @throws NullPointerException
	 *             if a required key is missing or the file is null
	 */
	protected Properties loadProperties(File file, String... requiredKeys)
			throws IOException, NullPointerException {
		logger.debug("Loading properties from " + file.getCanonicalPath());
		Properties props = new Properties();
		try (FileReader fr = new FileReader(file)) {
			props.load(fr);
		}

		for (String reqKey : requiredKeys) {
			if (!props.containsKey(reqKey))
				throw new NullPointerException(file.getName()
						+ " is missing key " + reqKey);
		}
		return props;
	}

	/**
	 * Loads a reply string from a file, ignoring lines prefixed with a hash-tag
	 * (#). Any empty lines prior to the first non-empty line are ignored. Lines
	 * are separated Unix-like (only \n)
	 * 
	 * @param file
	 *            the file to load from
	 * @return the reply format
	 * @throws IOException
	 *             if an i/o exception occurs, like the file not existing
	 */
	protected String loadReplyString(File file) throws IOException {
		logger.debug("Loading reply string from " + file.getCanonicalPath());
		String result = "";
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String ln;
			boolean first = true;
			while ((ln = br.readLine()) != null) {
				if (ln.startsWith("#") || (first && ln.isEmpty()))
					continue;
				if (!first) {
					result += "\n";
				} else {
					first = false;
				}
				result += ln;
			}
		}
		return result;
	}

	/**
	 * Loads a list of strings from the file, where there is 1 string per line.
	 * Lines prefixed with /u/ are modified as if by
	 * {@link java.lang.String#substring(int)} with parameter 3. All strings
	 * are lower-cased.
	 * 
	 * @param file
	 *            the file to load from
	 * @return each line in the file
	 * @throws IOException
	 *             if an i/o exception occurs, like the file not existing
	 */
	protected List<String> loadStringList(File file) throws IOException {
		logger.debug("Loading string list from " + file.getCanonicalPath());
		List<String> result = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String ln;
			while ((ln = br.readLine()) != null) {
				if (ln.startsWith("/u/")) {
					ln = ln.substring(3);
				}
				result.add(ln.toLowerCase());
			}
		}
		return result;
	}
	
	/**
	 * @return the database info, containing url, user-name & password
	 */
	public Properties getDatabaseInfo() {
		return databaseInfo;
	}

	/**
	 * @return the message format when someone makes a succesful loan
	 */
	public String getSuccessfulLoan() {
		return successfulLoan;
	}

	/**
	 * @return the message format when someone gets paid back by someone who never asked for a loan
	 */
	public String getNoLoansToRepay() {
		return noLoansToRepay;
	}

	/**
	 * @return the message format when someone gets paid back
	 */
	public String getRepayment() {
		return repayment;
	}

	/**
	 * @return the format for a loan
	 */
	public String getLoanFormat() {
		return loanFormat;
	}

	/**
	 * @return the message format for a check
	 */
	public String getCheck() {
		return check;
	}

	/**
	 * @return the message when an action (such as a check) is performed on a banned user
	 */
	public String getActionToBanned() {
		return actionToBanned;
	}

	/**
	 * @param actionToBanned the actionToBanned to set
	 */
	public void setActionToBanned(String actionToBanned) {
		this.actionToBanned = actionToBanned;
	}

	/**
	 * @return the checkTruncated
	 */
	public String getCheckTruncated() {
		return checkTruncated;
	}

	/**
	 * @param checkTruncated the checkTruncated to set
	 */
	public void setCheckTruncated(String checkTruncated) {
		this.checkTruncated = checkTruncated;
	}

	/**
	 * @return the unpaid
	 */
	public String getUnpaid() {
		return unpaid;
	}

	/**
	 * @param unpaid the unpaid to set
	 */
	public void setUnpaid(String unpaid) {
		this.unpaid = unpaid;
	}

	/**
	 * @return the confirm
	 */
	public String getConfirm() {
		return confirm;
	}

	/**
	 * @param confirm the confirm to set
	 */
	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

}
