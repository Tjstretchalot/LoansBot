package me.timothy.bots.summon;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.currencies.CurrencyHandler;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.jreddit.info.Comment;

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
			.compile("(\\s*\\$confirm\\s/u/\\S+\\s" + BotUtils.getDollarAmountPatternString() + ")([A-Z]{3})?");
	
	private static final String CONFIRM_FORMAT = "$confirm <user1> <money1>";

	private Logger logger;

	public ConfirmSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(comment.author().equalsIgnoreCase(config.getProperty("user.username")))
			return null;
		
		Matcher matcher = CONFIRM_PATTERN.matcher(comment.body());
		if(matcher.find()) {
			String text = matcher.group(1).trim();
			ResponseInfo ri = ResponseInfoFactory.getResponseInfo(CONFIRM_FORMAT, text, comment);
			
			String borrower = ri.getObject("author").toString().toLowerCase();
			String lender = ri.getObject("user1").toString().toLowerCase();
			int money = ((MoneyFormattableObject) ri.getObject("money1")).getAmount();
			if(borrower.equals(lender)) {
				logger.printf(Level.INFO, "Ignoring %s confirming he sent money to himself!", borrower);
				return null;
			}
			
			final int originalMoneyPennies = money;
			boolean hasConversion = matcher.group(2) != null;
			String convertFrom = "USD";
			double conversionRate = 1;
			if(hasConversion) {
				convertFrom = matcher.group(2).trim();
				conversionRate = CurrencyHandler.getInstance().getConversionRate(convertFrom, "USD");
				logger.debug("Converting from " + convertFrom + " to USD using rate " + conversionRate);
				money *= conversionRate;
			}
		
			logger.printf(Level.INFO, "%s confirmed a $%s transfer from %s", borrower,
					money, lender);

			LoansDatabase database = (LoansDatabase) db;
			
			Username lenderUsername = database.getUsernameMapping().fetchByUsername(lender);
			Username borrowerUsername = database.getUsernameMapping().fetchByUsername(borrower);

			boolean validConfirm = false;
			int numLoans = 0;
			if(lenderUsername != null && borrowerUsername != null) {
				User lenderUser = database.getUserMapping().fetchById(lenderUsername.userId);
				User borrowerUser = database.getUserMapping().fetchById(borrowerUsername.userId);
					
				List<Loan> loans = database.getLoanMapping().fetchWithBorrowerAndOrLender(borrowerUser.id, lenderUser.id, true);
				numLoans = loans.size();
				for(Loan loan : loans) {
					if(loan.principalCents != loan.principalRepaymentCents && loan.principalCents >= money) {
						validConfirm = true;
					}
				}
			}
			ri.addTemporaryString("numloans", Integer.toString(numLoans));
			
			if(hasConversion) {
				ri.addTemporaryString("convert_from", convertFrom);
				ri.addTemporaryString("conversion_rate", Double.toString(conversionRate));
				ri.addTemporaryObject("original_money", new MoneyFormattableObject(originalMoneyPennies));
				ri.addTemporaryObject("money1", new MoneyFormattableObject(money));
			}
			
			String responseName = "confirm";
			if(!validConfirm) { responseName += "NoLoan"; }
			if(hasConversion) { responseName += "HasConversion"; }
			
			ResponseFormatter formatter = new ResponseFormatter(database.getResponseMapping().fetchByName(responseName).responseBody, ri);
			
			return new SummonResponse(SummonResponse.ResponseType.VALID, formatter.getFormattedResponse(config, (LoansDatabase) db), "991c8042-3ecc-11e4-8052-12313d05258a");
		}
		return null;
	}
}
