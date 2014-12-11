package me.timothy.bots.summon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	
	private String applyChanges(FileConfiguration config, Database db) {
		LoansDatabase database = (LoansDatabase) db;
		if(config.getList("banned").contains(doneTo.toLowerCase())) {
			logger.info("Someone is attempting to check a banned user");
			return config.getString("action_to_banned");
		}

		logger.printf(Level.INFO, "%s requested a check on %s", doer, doneTo);
		User user = database.getUserByUsername(doneTo);
		
		List<Loan> relevantLoans1 = user != null ? database.getLoansWithBorrowerAndOrLender(user.id, user.id, false) : new ArrayList<Loan>();

		return config.getString("check")
				.replace("<checker>", doer)
				.replace("<user>", doneTo)
				.replace("<loans>", LoansBotUtils.getLoansString(relevantLoans1, database, doneTo, config))
				.replace("<applied>", (user != null && user.claimed) ? "Yes" : "No");
	}
	@Override
	public SummonResponse handleLink(Link submission, Database db, FileConfiguration config) {
		String title = submission.title();
		if(title.toUpperCase().startsWith("[META]"))
			return null;
		
		
		this.doer = "AUTOMATIC";
		this.doneTo = submission.author();
		return new SummonResponse(SummonResponse.ResponseType.VALID, applyChanges(config, db));
	}
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = CHECK_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String text = matcher.group().trim();
			
			this.doer = comment.author();
			this.doneTo = BotUtils.getUser(text.split("\\s")[1]);
			return new SummonResponse(SummonResponse.ResponseType.VALID, applyChanges(config, db));
		}
		return null;
	}

}
