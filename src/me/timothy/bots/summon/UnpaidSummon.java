package me.timothy.bots.summon;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnpaidSummon extends Summon {
	/**
	 * Matches things like
	 * 
	 * $unpaid /u/John
	 * $unpaid /u/Asdf_Jkl
	 */
	private static final Pattern UNPAID_PATTERN = Pattern.compile("\\s*\\$unpaid\\s/u/\\S+");

	private Logger logger;

	private String doer;
	private String doneTo;

	public UnpaidSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public String applyChanges(FileConfiguration config, Database database) {
		try {
			if(config.getBannedUsers().contains(doneTo.toLowerCase())) {
				logger.info("Someone is attempting to $unpaid a banned user");
				return config.getActionToBanned();
			}

			List<Loan> relevantLoans = database.getLoansWith(doer, doneTo);
			List<Loan> changed = new ArrayList<>();

			for(Loan l : relevantLoans) {
				if(l.getAmountPaidPennies() != l.getAmountPennies()) {
					database.setLoanUnpaid(l.getId(), true);
					l.setUnpaid(true);
					changed.add(l);
				}
			}

			logger.printf(Level.INFO, "%s has defaulted on %d loans from %s", doneTo, changed.size(), doer);

			return config.getUnpaid().replace("<lender>", doer).replace("<borrower>", doneTo).replace("<loans>", LoansBotUtils.getLoansStringRaw(changed, config));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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

}
