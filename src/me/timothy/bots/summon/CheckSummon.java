package me.timothy.bots.summon;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;

public class CheckSummon extends Summon {
	/**
	 * Matches things like
	 * 
	 * $check /u/John $check /u/Asdf_Jkl
	 */
	private static final Pattern CHECK_PATTERN = Pattern
			.compile("\\s*\\$check\\s/u/\\S+");

	private Logger logger;
	private String doer;
	private String doneTo;

	public CheckSummon() {
		logger = LogManager.getLogger();
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

	@Override
	public String applyChanges(FileConfiguration config, Database database) {
		try {
			if(config.getBannedUsers().contains(doneTo.toLowerCase())) {
				logger.info("Someone is attempting to check a banned user");
				return config.getActionToBanned();
			}

			logger.printf(Level.INFO, "%s requested a check on %s", doer, doneTo);
			List<Loan> relevantLoans1 = database.getLoansWith(doneTo);

			return config.getCheck()
					.replace("<checker>", doer)
					.replace("<user>", doneTo)
					.replace("<loans>", LoansBotUtils.getLoansString(relevantLoans1, doneTo, config));
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

}
