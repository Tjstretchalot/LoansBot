package me.timothy.bots;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.models.CreationInfo;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.summon.PMResponse;

/**
 * Contains various utility static methods
 * that are necessary for the loans bot.
 * 
 * @author Timothy
 *
 */
public class LoansBotUtils {
	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * The primary subreddit the bot is for
	 */
	public static final String PRIMARY_SUBREDDIT = "borrow";
	
	/**
	 * The secondary subreddits the bot is for
	 */
	public static final List<String> SECONDARY_SUBREDDITS = Collections.unmodifiableList(Arrays.asList("loansbot", "borrowcommands"));

	/*
	 * Acquired from http://stackoverflow.com/questions/6710094/how-to-format-an-elapsed-time-interval-in-hhmmss-sss-format-in-java
	 */
	/**
	 * Format the interval in milliseconds to hours, minutes, seconds, and milliseconds
	 * @param l the interval in milliseconds
	 * @return the formatted version
	 */
	public static String formatInterval(final long l)
	{
		final long hr = TimeUnit.MILLISECONDS.toHours(l);
		final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}
	
	/**
	 * Format the given timestamp in a really detailed way using simple date format
	 * 
	 * Looks like Saturday November 2012 10:45:42.720+0100
     * 
	 * @param timestamp the timestamp you want to format
	 * @return the formatted representation of the timestamp as best as possible
	 */
	public static String formatDate(Timestamp timestamp) {
		Date dt = new Date(timestamp.getTime());
		SimpleDateFormat formatter = new SimpleDateFormat("EEEEE MMMMM yyyy HH:mm:ss.SSSZ");
		return formatter.format(dt);
	}
	
	/**
	 * Gets a string that represents the specified loan such that it is readily
	 * readable. If there are less than 5 loans this is equivalent to getLoansStringRaw,
	 * otherwise it matches config.getCheckTruncated
	 * 
	 * @param loans the loans
	 * @param db the database
	 * @param relevantUser the relevant user in the loans (required for truncating)
	 * @param config the configuration options to use
	 * @return a string representing a human-readable version of the list of loans. Potentially truncated
	 */
	public static String getLoansString(List<Loan> loans, LoansDatabase db, String relevantUser, FileConfiguration config) {
		if(loans == null || loans.size() == 0) {
			return "No History\n\n";
		}
		

		String loansString = "";
		if(loans.size() < 5) {
			loansString = getLoansAsTable(loans, db, 5);
		}else {
			
			List<Loan> asBorrower = getLoansWithBorrower(loans, db, relevantUser);
			List<Loan> asLender = getLoansWithLender(loans, db, relevantUser);
			List<Loan> unpaidAsBorrower = getUnpaidLoans(asBorrower);
			List<Loan> unpaidAsLender = getUnpaidLoans(asLender);
			List<Loan> asBorrowerDone = getPaidLoans(asBorrower);
			List<Loan> asLenderDone = getPaidLoans(asLender);
			
			List<Loan> inprogressAsBorrower = asBorrower;
			inprogressAsBorrower.removeAll(unpaidAsBorrower);
			inprogressAsBorrower.removeAll(asBorrowerDone);
			
			List<Loan> inprogressAsLender = asLender;
			inprogressAsLender.removeAll(unpaidAsLender);
			inprogressAsLender.removeAll(asLenderDone);
			
			long amountBorrowedDonePen = getTotalLentPen(asBorrowerDone);
			long amountLenderDonePen = getTotalLentPen(asLenderDone);
			
			loansString = db.getResponseMapping().fetchByName("check_truncated").responseBody;
			
			loansString = loansString.replace("<relevant user>", relevantUser);
			loansString = loansString.replace("<num borrowed done>", Integer.toString(asBorrowerDone.size()));
			loansString = loansString.replace("<amount borrowed done>", BotUtils.getCostString(amountBorrowedDonePen / 100.));
			loansString = loansString.replace("<num lended done>", Integer.toString(asLenderDone.size()));
			loansString = loansString.replace("<amount lended done>", BotUtils.getCostString(amountLenderDonePen / 100.));
			loansString = loansString.replace("<loans unpaid borrower>", getLoansAsTable(unpaidAsBorrower, db, 3));
			loansString = loansString.replace("<loans unpaid lender>", getLoansAsTable(unpaidAsLender, db, 3));
			loansString = loansString.replace("<loans inprogress borrower>", getLoansAsTable(inprogressAsBorrower, db, 20));
			loansString = loansString.replace("<loans inprogress lender>", getLoansAsTable(inprogressAsLender, db, 20));
		}
		
		return loansString;
	}
	
	
	/**
	 * Sorts the specified list of loans according to their date (newest first),
	 * and adds a little note at the bottom if any loans are truncated
	 * @param loans the loans to stringify
	 * @param db the database to use if additional information is needed
	 * @param max the maximum loans before truncation
	 * @return the loans as a table
	 */
	public static String getLoansAsTable(List<Loan> loans, LoansDatabase db, int max) {
		Collections.sort(loans, new Comparator<Loan>() {

			@Override
			public int compare(Loan o1, Loan o2) {
				// We want a DESCENDING sort (instead of ascending) so o2 compares to o1
				return Long.valueOf(o2.createdAt.getTime()).compareTo(o1.createdAt.getTime());
			}
			
		});
		
		Table table = new Table(Table.Alignment.LEFT, "Lender", "Borrower", "Amount Given", 
				"Amount Repaid", "Unpaid?", "Original Thread", "Date Given", "Date Paid Back");
		
		int toShow = loans.size() > max ? max : loans.size();
		for(int i = 0; i < toShow; i++) {
			Loan l = loans.get(i);
			User uLend = db.getUserMapping().fetchById(l.lenderId);
			User uBorr = db.getUserMapping().fetchById(l.borrowerId);
			CreationInfo cInfo = db.getCreationInfoMapping().fetchByLoanId(l.id);
			
			
			table.addRow(getUsernamesSeperatedWith(uLend, db, " aka "), getUsernamesSeperatedWith(uBorr, db, " aka "), BotUtils.getCostString(l.principalCents/100.), 
					BotUtils.getCostString(l.principalRepaymentCents/100.), l.unpaid ? "***UNPAID***" : "", 
							(cInfo != null && cInfo.type == CreationInfo.CreationType.REDDIT) ? String.format("[Original Thread](%s)", cInfo.thread) : "",
							l.createdAt != null ? BotUtils.getDateStringFromJUTC(l.createdAt.getTime()) : "",
							(l.principalRepaymentCents == l.principalCents && l.updatedAt != null) ? BotUtils.getDateStringFromJUTC(l.updatedAt.getTime()) : "");
		}
		
		StringBuilder result = new StringBuilder(table.format());
		
		if(toShow != loans.size()) {
			List<Loan> skipped = new ArrayList<>();
			for(int i = toShow; i < loans.size(); i++) {
				skipped.add(loans.get(i));
			}
			result.append("\n");
			result.append("- + An additional " + skipped.size() + " older loans (a total of $" +
						BotUtils.getCostString(getTotalLentPen(skipped) / 100.) + " lent) that were truncated\n");
		}
		return result.toString();
	}
	
	/**
	 * <p>Fetches the username of the specified user and returns them separated
	 * with the specified string.</p>
	 * 
	 * <p>Ex. if the user had the username Bob and no other usernames, and this was called
	 * with the separator " / ", then the result would be Bob</p>
	 * 
	 * <p>Ex. if the username had the usernames Bob and Alice and no other usernames, and this
	 * was called with the separator " / ", then the result would be Bob / Alice</p>
	 * @param user the user to fetch usernames of
	 * @param db the database to use to fetch the usernames
	 * @param separator the separator between usernames
	 * @return the usernames separated with the specified separator
	 */
	public static String getUsernamesSeperatedWith(User user, LoansDatabase db, String separator) {
		List<Username> usernames = db.getUsernameMapping().fetchByUserId(user.id);
		return formatUsernamesSeparatedWith(usernames, separator, false);
	}
	
	/**
	 * Returns the usernames formatted with the given separator. Optionally the users are formatted
	 * to create a link in reddit to their account.
	 * 
	 * @param usernames The usernames to format
	 * @param seperator the separator between usernames
	 * @param link if true, the usernames will be formatted such that they appear as a link on reddit
	 * @return the formatted usernames
	 */
	public static String formatUsernamesSeparatedWith(List<Username> usernames, String seperator, boolean link) {
		StringBuilder result = new StringBuilder();
		for(int i = 0, len = usernames.size(); i < len; i++) {
			if(i > 0)
				result.append(seperator);
			
			if(link)
				result.append("/u/");
			result.append(usernames.get(i).username);
		}
		return result.toString();
	}
	
	/**
	 * Searches a big list of loans and selectively grabs the loans where
	 * {@code borrower} is the borrower
	 * 
	 * @param bigList the big list of loans
	 * @param db the database to get more info from
	 * @param borrower the borrower to search for
	 * @return a list of loans from {@code bigList} where the borrower is {@code borrower}
	 */
	private static List<Loan> getLoansWithBorrower(List<Loan> bigList, LoansDatabase db, String borrower) {
		List<Loan> result = new ArrayList<>();
		for(Loan l : bigList) {
			List<Username> borrowerUsernames = db.getUsernameMapping().fetchByUserId(l.borrowerId);
			for(Username borrowerUsername : borrowerUsernames) {
				if(borrowerUsername.username.equalsIgnoreCase(borrower)) {
					result.add(l);
				}
			}
		}
		return result;
	}
	/**
	 * Searches a big list of loans and selectively grabs the loans where
	 * {@code lender} is the lender
	 * 
	 * @param bigList the big list of loans
	 * @param db the database to get more info from
	 * @param lender the lender to search for
	 * @return a list of loans from {@code bigList} where the lender is {@code lender}
	 */
	private static List<Loan> getLoansWithLender(List<Loan> bigList, LoansDatabase db, String lender) {
		List<Loan> result = new ArrayList<>();
		for(Loan l : bigList) {
			List<Username> lenderUsernames = db.getUsernameMapping().fetchByUserId(l.lenderId);
			for(Username lenderUsername : lenderUsernames) {
				if(lenderUsername.username.equalsIgnoreCase(lender)) {
					result.add(l);
				}
			}
		}
		return result;
	}
	
	/**
	 * Searches a list of loans and selectively grabs the loans that are
	 * unpaid
	 * 
	 * @param bigList the big list of loans
	 * @return loans from {@code bigList} that are unpaid
	 */
	private static List<Loan> getUnpaidLoans(List<Loan> bigList) {
		List<Loan> result = new ArrayList<>();
		for(Loan l : bigList) {
			if(l.unpaid) {
				result.add(l);
			}
		}
		return result;
	}
	
	/**
	 * Searches a big list of loans and selectively grabs the loans
	 * that are paid (amount lended == amount returned)
	 * 
	 * @param bigList the big list of loans
	 * @return loans from {@code bigList} that are fully paid off
	 */
	private static List<Loan> getPaidLoans(List<Loan> bigList) {
		List<Loan> result = new ArrayList<>();
		for(Loan l : bigList) {
			if(l.principalCents == l.principalRepaymentCents) {
				result.add(l);
			}
		}
		return result;
	}
	
	/**
	 * Calculates the total amount of money lent in a list of loans
	 * 
	 * @param loans the list of loans to sum
	 * @return the amount lent in {@code loans} in pennies
	 */
	private static long getTotalLentPen(List<Loan> loans) {
		long total = 0;
		for(Loan l : loans) {
			total += l.principalCents;
		}
		return total;
	}
	
	/**
	 * Convenience function to construct a pm response to the given user using
	 * the given format and response info.
	 * 
	 * @param to Who is receiving the pm
	 * @param titleFmt the format for the title
	 * @param bodyFmt the format for the body
	 * @param db the loans database
	 * @param cfg the file configuration
	 * @param respInfo contains valid substitutions within the format
	 * @return the formatted pm response
	 */
	public static PMResponse createPMFromFormat(
			String to, String titleFmt, String bodyFmt, LoansDatabase db,
			FileConfiguration cfg, ResponseInfo respInfo) {
		String pmTitleFmt = db.getResponseMapping().fetchByName(titleFmt).responseBody;
		String pmBodyFmt = db.getResponseMapping().fetchByName(bodyFmt).responseBody;
		
		String pmTitle = new ResponseFormatter(pmTitleFmt, respInfo).getFormattedResponse(cfg, db);
		String pmBody = new ResponseFormatter(pmBodyFmt, respInfo).getFormattedResponse(cfg, db);
		return new PMResponse(to, pmTitle, pmBody);
	}
	
	/**
	 * Returns the pm that should be sent when an exception occurs in a portion
	 * of code which is not essential to the loansbot operations, such as 
	 * formatting a response.
	 * 
	 * @param source a short identifying string about what we were trying to do
	 * @param e the exception that was thrown
	 * @return the pm response to notify the moderators
	 */
	public static PMResponse exceptionPM(String source, Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		return new PMResponse(
				"/r/" + PRIMARY_SUBREDDIT,
				"LoansBot - " + source + " - Non-critical Error",
				"An exception was raised in a noncritical portion of code. "
				+ "Since the location was noncritical the bot will continue "
				+ "to function as normal.\n\n```\n" + sw.toString() + "\n```"
				);
	}
}
