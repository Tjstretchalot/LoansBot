package me.timothy.bots.responses;

import java.util.ArrayList;
import java.util.List;

import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;

/**
 * If you have user1, you can also have loans1 automatically
 * work
 * 
 * @author Timothy
 */
public class LoanFormattableObject implements FormattableObject {

	@Override
	public String toFormattedString(ResponseInfo info, String myName, FileConfiguration config, LoansDatabase db) {
		String username = info.getObject(myName.replace("loans", "user")).toString();
		User userToGetLoansOf = db.getUserByUsername(username);
		List<Loan> relevantLoans1 = userToGetLoansOf != null ? db.getLoansWithBorrowerAndOrLender(userToGetLoansOf.id, userToGetLoansOf.id, false) : new ArrayList<Loan>();
		
		return LoansBotUtils.getLoansString(relevantLoans1, db, username, config);
	}

}
