package me.timothy.bots.summon;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansFileConfiguration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jreddit.comment.Comment;
import com.github.jreddit.message.Message;

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
	
	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.comment.Comment)
	 */
	@Override
	public boolean parse(Comment comment) throws UnsupportedOperationException {
		return parse(comment.getAuthor(), comment.getComment());
	}

	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.message.Message)
	 */
	@Override
	public boolean parse(Message message) throws UnsupportedOperationException {
		return parse(message.getAuthor(), message.getBody());
	}

	private boolean parse(String author, String text) {
		Matcher matcher = UNPAID_PATTERN.matcher(text);
		
		if(matcher.find()) {
			String group = matcher.group().trim();
			String[] split = group.split("\\s");
			
			this.doer = author;
			this.doneTo = BotUtils.getUser(split[1]);
			
			return true;
		}
		return false;
	}

	@Override
	public String applyChanges(FileConfiguration con, Database db) {
		LoansFileConfiguration config = (LoansFileConfiguration) con;
		LoansDatabase database = (LoansDatabase) db;
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
