package me.timothy.bots.summon;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.Loan;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;

/**
 * A summon for checking all loans made by a user
 * 
 * @author Timothy
 */
public class CheckSummon implements CommentSummon, LinkSummon {
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

	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.comment.Comment)
	 */
	@Override
	public boolean parse(Comment comment) throws UnsupportedOperationException {
		Matcher matcher = CHECK_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String text = matcher.group().trim();
			
			this.doer = comment.author();
			this.doneTo = BotUtils.getUser(text.split("\\s")[1]);
			return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(com.github.jreddit.submissions.Submission)
	 */
	@Override
	public boolean parse(Link submission)
			throws UnsupportedOperationException {
		
		String title = submission.title();
		if(title.toUpperCase().startsWith("[META]"))
			return false;
		
		
		this.doer = "AUTOMATIC";
		this.doneTo = submission.author();
		
		return true;
	}
	
	@Override
	public String applyChanges(FileConfiguration con, Database db) {
		LoansFileConfiguration config = (LoansFileConfiguration) con;
		LoansDatabase database = (LoansDatabase) db;
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
					.replace("<loans>", LoansBotUtils.getLoansString(relevantLoans1, doneTo, config))
					.replace("<applied>", database.getApplicantByUsername(doneTo).size() > 0 ? "Yes" : "No");
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

}
