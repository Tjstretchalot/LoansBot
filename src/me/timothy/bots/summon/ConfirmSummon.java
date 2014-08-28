package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;

public class ConfirmSummon extends Summon {
	/**
	 * Matches things like
	 * 
	 * $unpaid /u/John $unpaid /u/Asdf_Jkl
	 */
	private static final Pattern CONFIRM_PATTERN = Pattern
			.compile("\\s*\\$confirm\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?");

	private Logger logger;
	
	private String doer;
	private String doneTo;
	private int amountPennies;

	public ConfirmSummon() {
		super(SummonType.CONFIRM, CONFIRM_PATTERN);
		
		logger = LogManager.getLogger();
	}

	@Override
	public void parse(String doer, String doneTo, String url, String text)
			throws ParseException {
		this.doer = doer;

		if (doer == null)
			throw new IllegalArgumentException("Confirm summons require a doer");

		String[] split = text.split("\\s");
		this.doneTo = getUser(split[1]);
		String number = split[2].replace("$", "");

		amountPennies = getPennies(number);

		if (amountPennies <= 0)
			throw new ParseException("Negative amount of money",
					split[0].length() + split[1].length() + 2);
	}

	@Override
	public String applyChanges(FileConfiguration config, Database database)
			throws SQLException {
		if(config.getBannedUsers().contains(doneTo.toLowerCase())) {
			logger.info("Someone is attempting to $confirm a banned user");
			return config.getActionToBanned();
		}
		logger.printf(Level.INFO, "%s confirmed a $%s transfer from %s", doer, LoansBotUtils.getCostString(amountPennies /100.), doneTo);
		
		return config.getConfirm().replace("<borrower>", doer).replace("<lender>", doneTo).replace("<amount>", LoansBotUtils.getCostString(amountPennies / 100.));
	}

	/**
	 * @return the doer
	 */
	public String getDoer() {
		return doer;
	}

	/**
	 * @return the doneTo
	 */
	public String getDoneTo() {
		return doneTo;
	}

	/**
	 * @return the amountPennies
	 */
	public int getAmountPennies() {
		return amountPennies;
	}

}
