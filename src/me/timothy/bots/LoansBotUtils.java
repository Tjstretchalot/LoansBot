package me.timothy.bots;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoansBotUtils {
	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger();
	/**
	 * Gets a string that represents the specified loan such that it is readily
	 * readable. If there are less than 5 loans this is equivalent to getLoansStringRaw,
	 * otherwise it matches config.getCheckTruncated
	 * 
	 * @param loans the loans
	 * @param relevantUser the relevent user in the loans (required for truncating)
	 * @param config the configuration options to use
	 * @return a string representing a human-readable version of the list of loans. Potentially truncated
	 */
	public static String getLoansString(List<Loan> loans, String relevantUser, FileConfiguration config) {
		if(loans == null || loans.size() == 0) {
			return "No History\n\n";
		}
		

		String loansString = "";
		if(loans.size() < 5) {
			loansString = getLoansStringRaw(loans, config);
		}else {
			List<Loan> inprogressAsBorrower = new ArrayList<>();
			List<Loan> inprogressAsLender = new ArrayList<>();
			List<Loan> unpaidAsBorrower = new ArrayList<>();
			List<Loan> unpaidAsLender = new ArrayList<>();
			List<Loan> asBorrowerDone = new ArrayList<>();
			List<Loan> asLenderDone = new ArrayList<>();
			
			for(Loan l : loans) {
				boolean borrower = relevantUser.equals(l.getBorrower());
				int state = l.getAmountPennies() == l.getAmountPaidPennies() ? 2 : (l.isUnpaid() ? 1 : 0);
				switch(state) {
				case 0:
					if(borrower)
						inprogressAsBorrower.add(l);
					else
						inprogressAsLender.add(l);
					break;
				case 1:
					if(borrower)
						unpaidAsBorrower.add(l);
					else
						unpaidAsLender.add(l);
					break;
				case 2:
					if(borrower)
						asBorrowerDone.add(l);
					else
						asLenderDone.add(l);
					break;
				}
			}
			
			long amountBorrowedDonePen = 0;
			for(Loan l : asBorrowerDone)
				amountBorrowedDonePen += l.getAmountPaidPennies();
			
			long amountLenderDonePen = 0;
			for(Loan l : asLenderDone)
				amountLenderDonePen += l.getAmountPaidPennies();
			
			loansString = config.getCheckTruncated();
			
			loansString = loansString.replace("<relevant user>", relevantUser);
			loansString = loansString.replace("<num borrowed done>", Integer.toString(asBorrowerDone.size()));
			loansString = loansString.replace("<amount borrowed done>", BotUtils.getCostString(amountBorrowedDonePen / 100.));
			loansString = loansString.replace("<num lended done>", Integer.toString(asLenderDone.size()));
			loansString = loansString.replace("<amount lended done>", BotUtils.getCostString(amountLenderDonePen / 100.));
			loansString = loansString.replace("<loans unpaid borrower>", getLoansStringRaw(unpaidAsBorrower, config));
			loansString = loansString.replace("<loans unpaid lender>", getLoansStringRaw(unpaidAsLender, config));
			loansString = loansString.replace("<loans inprogress borrower>", getLoansStringRaw(inprogressAsBorrower, config));
			loansString = loansString.replace("<loans inprogress lender>", getLoansStringRaw(inprogressAsLender, config));
		}
		
		return loansString;
	}
	
	/**
	 * Formats a list of loans such that there is 1 loan seperated by 2 newlines,
	 * where each loan is formated according to getLoanString. Always ends in 2
	 * newlines. In the event of an empty list, returns "No History\n\n"
	 * 
	 * @param loans the list of loans
	 * @param config the configuration options to use
	 * @return a somewhat human-readable version of loans
	 */
	public static String getLoansStringRaw(List<Loan> loans,
			FileConfiguration config) {
		if(loans == null || loans.size() == 0) {
			return "No History\n\n";
		}
		String loansString = "";
		for(Loan l : loans) {
			loansString += getLoanString(l, config) + "\n\n";
		}
		return loansString;
	}

	/**
	 * Formats the specified loan according to config.getLoanFormat
	 * @param l the loan to format
	 * @param config the configuration options to use
	 * @return a human-readable version of the loan
	 */
	public static String getLoanString(Loan l, FileConfiguration config) {
		String thisLoanString = config.getLoanFormat();
		thisLoanString = thisLoanString.replace("<borrower>", l.getBorrower());
		thisLoanString = thisLoanString.replace("<lender>", l.getLender());
		thisLoanString = thisLoanString.replace("<amount initial>", BotUtils.getCostString(l.getAmountPennies()/100.));
		thisLoanString = thisLoanString.replace("<amount paid>", BotUtils.getCostString(l.getAmountPaidPennies()/100.));
		if(l.getOriginalThread() != null) {
			thisLoanString = thisLoanString.replace("<initial thread link>", "[Original Thread](" + l.getOriginalThread() + ")");
		}else {
			thisLoanString = thisLoanString.replaceAll("<initial thread link>", "");
		}
		thisLoanString = thisLoanString.replace("<unpaid>", l.isUnpaid() ? "***UNPAID***" : "");
		thisLoanString = thisLoanString.replace("<loan date>", l.getDateLoanGivenJUTC() == 0 ? "" : "Loan given at: " + BotUtils.getDateStringFromJUTC(l.getDateLoanGivenJUTC()));
		thisLoanString = thisLoanString.replace("<paid back date>", l.getDatePaidBackFullJUTC() == 0 ? "" : "Loan paid in full at: " + BotUtils.getDateStringFromJUTC(l.getDateLoanGivenJUTC()));
		return thisLoanString;
	}
}
