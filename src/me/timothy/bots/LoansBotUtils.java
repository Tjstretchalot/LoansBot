package me.timothy.bots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	 * @param relevantUser the relevant user in the loans (required for truncating)
	 * @param config the configuration options to use
	 * @return a string representing a human-readable version of the list of loans. Potentially truncated
	 */
	public static String getLoansString(List<Loan> loans, String relevantUser, LoansFileConfiguration config) {
		if(loans == null || loans.size() == 0) {
			return "No History\n\n";
		}
		

		String loansString = "";
		if(loans.size() < 5) {
			loansString = getLoansAsTable(loans, 5);
		}else {
			List<Loan> inprogressAsBorrower = new ArrayList<>();
			List<Loan> inprogressAsLender = new ArrayList<>();
			List<Loan> unpaidAsBorrower = new ArrayList<>();
			List<Loan> unpaidAsLender = new ArrayList<>();
			List<Loan> asBorrowerDone = new ArrayList<>();
			List<Loan> asLenderDone = new ArrayList<>();
			
			for(Loan l : loans) {
				boolean borrower = relevantUser.equals(l.getBorrower());
				boolean paid = l.getAmountPennies() == l.getAmountPaidPennies();
				boolean unpaid = l.isUnpaid();
				boolean inprog = !paid && !unpaid;
				if(borrower && inprog)
					inprogressAsBorrower.add(l);
				else if(!borrower && inprog)
					inprogressAsLender.add(l);
				else if(borrower && unpaid)
					unpaidAsBorrower.add(l);
				else if(!borrower && unpaid)
					unpaidAsLender.add(l);
				else if(borrower && paid)
					asBorrowerDone.add(l);
				else if(!borrower && paid)
					asLenderDone.add(l);
			}
			
			long amountBorrowedDonePen = 0;
			for(Loan l : asBorrowerDone) {
				amountBorrowedDonePen += l.getAmountPaidPennies();
			}
			
			long amountLenderDonePen = 0;
			for(Loan l : asLenderDone) {
				amountLenderDonePen += l.getAmountPaidPennies();
			}
			
			loansString = config.getCheckTruncated();
			
			loansString = loansString.replace("<relevant user>", relevantUser);
			loansString = loansString.replace("<num borrowed done>", Integer.toString(asBorrowerDone.size()));
			loansString = loansString.replace("<amount borrowed done>", BotUtils.getCostString(amountBorrowedDonePen / 100.));
			loansString = loansString.replace("<num lended done>", Integer.toString(asLenderDone.size()));
			loansString = loansString.replace("<amount lended done>", BotUtils.getCostString(amountLenderDonePen / 100.));
			loansString = loansString.replace("<loans unpaid borrower>", getLoansAsTable(unpaidAsBorrower, 3));
			loansString = loansString.replace("<loans unpaid lender>", getLoansAsTable(unpaidAsLender, 3));
			loansString = loansString.replace("<loans inprogress borrower>", getLoansAsTable(inprogressAsBorrower, 20));
			loansString = loansString.replace("<loans inprogress lender>", getLoansAsTable(inprogressAsLender, 20));
		}
		
		return loansString;
	}
	
	
	/**
	 * Sorts the specified list of loans according to their date (newest first),
	 * and adds a little note at the bottom if any loans are truncated
	 * @param loans
	 * @param max
	 * @return
	 */
	public static String getLoansAsTable(List<Loan> loans, int max) {
		Collections.sort(loans, new Comparator<Loan>() {

			@Override
			public int compare(Loan o1, Loan o2) {
				// We want a DESCENDING sort (instead of ascending) so o2 compares to o1
				return Long.valueOf(o2.getDateLoanGivenJUTC()).compareTo(o1.getDateLoanGivenJUTC());
			}
			
		});
		
		Table table = new Table(Table.Alignment.LEFT, "Lender", "Borrower", "Amount Given", 
				"Amount Repaid", "Unpaid?", "Original Thread", "Date Given", "Date Paid Back");
		
		int toShow = loans.size() > max ? max : loans.size();
		for(int i = 0; i < toShow; i++) {
			Loan l = loans.get(i);
			table.addRow(l.getLender(), l.getBorrower(), BotUtils.getCostString(l.getAmountPennies()/100.), 
					BotUtils.getCostString(l.getAmountPaidPennies()/100.), l.isUnpaid() ? "***UNPAID***" : "", 
							l.getOriginalThread() != null ? String.format("[Original Thread](%s)", l.getOriginalThread()) : "",
							l.getDateLoanGivenJUTC() != 0 ? BotUtils.getDateStringFromJUTC(l.getDateLoanGivenJUTC()) : "",
							l.getDatePaidBackFullJUTC() != 0 ? BotUtils.getDateStringFromJUTC(l.getDatePaidBackFullJUTC()) : "");
		}
		
		StringBuilder result = new StringBuilder(table.format());
		
		if(toShow != loans.size()) {
			result.append("\n");
			result.append("- + An additional " + (loans.size() - toShow) + " older loans that were truncated\n");
		}
		return result.toString();
	}
	
	/**
	 * Searches a big list of loans and selectively grabs the loans where
	 * {@code borrower} is the borrower
	 * 
	 * @param bigList the big list of loans
	 * @param borrower the borrower to search for
	 * @return a list of loans from {@code bigList} where the borrower is {@code borrower}
	 */
	private static List<Loan> getLoansWithBorrower(List<Loan> bigList, String borrower) {
		return null;
	}
	/**
	 * Searches a big list of loans and selectively grabs the loans where
	 * {@code lender} is the lender
	 * 
	 * @param bigList the big list of loans
	 * @param lender the lender to search for
	 * @return a list of loans from {@code bigList} where the lender is {@code lender}
	 */
	private static List<Loan> getLoansWithLender(List<Loan> bigList, String lender) {
		return null;
	}
	
	/**
	 * Searches a list of loans and selectively grabs the loans that are
	 * unpaid
	 * 
	 * @param bigList the big list of loans
	 * @return loans from {@code bigList} that are unpaid
	 */
	private static List<Loan> getUnpaidLoans(List<Loan> bigList) {
		return null;
	}
	
	/**
	 * Searches a big list of loans and selectively grabs the loans
	 * that are paid (amount lended == amount returned)
	 * 
	 * @param bigList the big list of loans
	 * @return loans from {@code bigList} that are fully paid off
	 */
	private static List<Loan> getPaidLoans(List<Loan> bigList) {
		return null;
	}
}
