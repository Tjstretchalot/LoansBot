package me.timothy.bots.summon;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.jreddit.info.Comment;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A summon for confirming that some money was transfered to someone
 * 
 * @author Timothy
 */
public class ConfirmSummon implements CommentSummon {
	/**
	 * Matches things like
	 * 
	 * $confirm /u/John $10
	 */
	private static final Pattern CONFIRM_PATTERN = Pattern
			.compile("\\s*\\$confirm\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?");
	
	private static final String CONFIRM_FORMAT = "$confirm <user1> <money1>";

	private Logger logger;

	public ConfirmSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = CONFIRM_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String text = matcher.group().trim();
			ResponseInfo ri = ResponseInfoFactory.getResponseInfo(CONFIRM_FORMAT, text, comment);
			
			String borrower = ri.getObject("author").toString().toLowerCase();
			String lender = ri.getObject("user1").toString().toLowerCase();
			int money = ((MoneyFormattableObject) ri.getObject("money1")).getAmount();
			if(borrower.equals(lender)) {
				logger.printf(Level.INFO, "Ignoring %s confirming he sent money to himself!");
				return null;
			}
		
			logger.printf(Level.INFO, "%s confirmed a $%s transfer from %s", borrower,
					money, lender);

			LoansDatabase database = (LoansDatabase) db;
			
			Username lenderUsername = database.getUsernameByUsername(lender);
			Username borrowerUsername = database.getUsernameByUsername(borrower);

			boolean validConfirm = false;
			int numLoans = 0;
			if(lenderUsername != null && borrowerUsername != null) {
				User lenderUser = database.getUserById(lenderUsername.userId);
				User borrowerUser = database.getUserById(borrowerUsername.userId);
					
				List<Loan> loans = database.getLoansWithBorrowerAndOrLender(borrowerUser.id, lenderUser.id, true);
				numLoans = loans.size();
				for(Loan loan : loans) {
					if(loan.principalCents != loan.principalRepaymentCents && loan.principalCents >= money) {
						validConfirm = true;
					}
				}
			}
			ri.addTemporaryString("numloans", Integer.toString(numLoans));
			
			ResponseFormatter formatter = new ResponseFormatter(database.getResponseByName(validConfirm ? "confirm" : "confirmNoLoan").responseBody, ri);
			
			return new SummonResponse(SummonResponse.ResponseType.VALID, formatter.getFormattedResponse(config, (LoansDatabase) db));
		}
		return null;
	}

}
