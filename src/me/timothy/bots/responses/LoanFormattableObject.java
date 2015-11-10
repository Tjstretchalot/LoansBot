package me.timothy.bots.responses;

import java.util.ArrayList;
import java.util.List;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Username;

/**
 * If you have user1, you can also have loans1 automatically
 * work
 * 
 * @author Timothy
 */
public class LoanFormattableObject implements FormattableObject {

	@Override
	public String toFormattedString(ResponseInfo info, String myName, FileConfiguration config, Database database) {
		LoansDatabase db = (LoansDatabase) database;
		String username = info.getObject(myName.replace("loans", "user")).toString();
		Username usernameModel = db.getUsernameByUsername(username);
		List<Loan> relevantLoans1 = new ArrayList<>();
		if(usernameModel != null) {
			relevantLoans1 = db.getLoansWithBorrowerAndOrLender(usernameModel.userId, usernameModel.userId, false);
		}
		return LoansBotUtils.getLoansString(relevantLoans1, db, username, config);
	}

}
