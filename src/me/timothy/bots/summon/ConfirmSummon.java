package me.timothy.bots.summon;

import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		logger = LogManager.getLogger();
	}

	@Override
	public String applyChanges(FileConfiguration config, Database database) {
		if(config.getBannedUsers().contains(doneTo.toLowerCase())) {
			logger.info("Someone is attempting to $confirm a banned user");
			return config.getActionToBanned();
		}
		logger.printf(Level.INFO, "%s confirmed a $%s transfer from %s", doer, BotUtils.getCostString(amountPennies /100.), doneTo);
		
		return config.getConfirm().replace("<borrower>", doer).replace("<lender>", doneTo).replace("<amount>", BotUtils.getCostString(amountPennies / 100.));
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
