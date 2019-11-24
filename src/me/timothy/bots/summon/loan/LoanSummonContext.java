package me.timothy.bots.summon.loan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.summon.PMResponse;

/**
 * Conceptually, this acts as both the event arguments for the loan created
 * event and the manager for what to do about it. We are not in a full
 * featured event bus environment (as that would be entirely unnecessary).\
 * 
 * In case we later want to split event arguments from response handling,
 * we don't allow reading the variables which are mutated (pmResponses,
 * specialResponses). If you don't already have a reference to them they
 * are black-box style. 
 * 
 * The above also prevents the order of events from easily influencing each
 * other as there is nothing exposed about previous events to new events,
 * unless the event hits the database.
 * 
 * @author Timothy
 *
 */
public class LoanSummonContext {
	/**
	 * The database we are working with
	 */
	public final LoansDatabase database;
	
	/**
	 * The current configuration options.
	 */
	public final FileConfiguration config;
	
	/**
	 * This is the lender - who made the request to create a new loan
	 */
	public final User lender;
	
	/**
	 * All usernames for the lender. Not modifiable.
	 */
	public final List<Username> lenderUsernames;
	
	/**
	 * This is the borrower - who the lender said is receiving the loan.
	 * This is implied by where the lender made his comment.
	 */
	public final User borrower;
	
	/**
	 * All usernames for the borrower. Not modifiable.
	 */
	public final List<Username> borrowerUsernames;
	
	/**
	 * The thread within which the comment was made 
	 */
	public final String requestThreadURL;
	
	/**
	 * How much the loan is worth in pennies
	 */
	public final int amountPennies;
	
	/**
	 * The private messages the loansbot will send out as a result of this loan
	 */
	private final List<PMResponse> pmResponses;
	
	/**
	 * Other special responses the loansbot will send out as a result of this
	 * loan
	 */
	private final HashMap<String, List<Object>> specialResponses;
	
	/**
	 * Creates a new loan summon context.
	 * 
	 * @param db the database to read/write from
	 * @param config the current configuration
	 * @param lender who created the loan
	 * @param borrower who received the loan
	 * @param requestThreadURL the url of the thread
	 * @param requestCommentURL the url of the comment
	 * @param pmResponses the responses list that will be filled
	 * @param specialResponses the special responses list that will be filled
	 */
	public LoanSummonContext(
			LoansDatabase db, FileConfiguration config, User lender, 
			User borrower, List<Username> lenderUsernames,
			List<Username> borrowerUsernames, String requestThreadURL, 
			int amountPennies, List<PMResponse> pmResponses, 
			HashMap<String, List<Object>> specialResponses) {
		this.database = db;
		this.config = config;
		this.lender = lender;
		this.borrower = borrower;
		this.lenderUsernames = Collections.unmodifiableList(lenderUsernames);
		this.borrowerUsernames = Collections.unmodifiableList(borrowerUsernames);
		this.requestThreadURL = requestThreadURL;
		this.amountPennies = amountPennies;
		this.pmResponses = pmResponses;
		this.specialResponses = specialResponses;
	}
	
	/**
	 * Ensure that the loansbot makes the specified personal message in 
	 * response to the loan command
	 * @param response the pm to make
	 */
	public void addPMResponse(PMResponse response) {
		pmResponses.add(response);
	}
	
	/**
	 * Add the given special response. Special responses use the key to
	 * determine what action to take. The type of the object depends
	 * on the key. See the package me.timothy.bots.specresps for details.
	 * @param key type of special response
	 * @param obj details of the special response
	 */
	public void addSpecialResponse(String key, Object obj) {
		List<Object> curVals = specialResponses.get(key);
		if(curVals == null) {
			curVals = new ArrayList<>();
			specialResponses.put(key,  curVals);
		}
		curVals.add(obj);
	}
}
