package me.timothy.bots.responses;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;


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
		Username username = db.getUsernameByUsername(info.getObject(myName.replace("applied", "user")).toString());
		User myUser = db.getUserById(username.userId);
		return (myUser != null && myUser.claimed) ? "Yes" : "No";
	}

}
