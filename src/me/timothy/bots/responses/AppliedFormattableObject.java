package me.timothy.bots.responses;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.User;


/**
 * If you have user1 then you can use applied1 to check if 
 * the user has gone through the application
 * 
 * @author Timothy
 *
 */
public class AppliedFormattableObject implements FormattableObject {

	@Override
	public String toFormattedString(ResponseInfo info, String myName, FileConfiguration config, Database database) {
		LoansDatabase db = (LoansDatabase) database;
		User myUser = db.getUserByUsername(info.getObject(myName.replace("applied", "user")).toString());
		return (myUser != null && myUser.claimed) ? "Yes" : "No";
	}

}
