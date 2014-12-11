package me.timothy.bots;

import java.io.IOException;

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
		
		addProperties("database", true, "url", "username", "password");
		
		addString("successful_loan", true);
		addString("no_loans_to_repay", true);
		addString("repayment", true);
		addString("action_to_banned", true);
		addString("check", true);
		addString("check_truncated", true);
		addString("unpaid", true);
		addString("confirm", true);
		addString("secondary_subreddit_postfix", true);
		addString("bad_verify_summon", true);
		addString("bad_loan_summon", true);
		addString("claim_code", true);
		addString("suicide", true);
	}
}
