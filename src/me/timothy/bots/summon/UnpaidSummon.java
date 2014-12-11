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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * For when someone does not pay back ones loan
 * in an adequate amount of time
 * 
 * @author Timothy
 */
public class UnpaidSummon implements CommentSummon {
	/**
	 * Matches things like
	 * 
	 * $unpaid /u/John
	 * $unpaid /u/Asdf_Jkl
	 */
	private static final Pattern UNPAID_PATTERN = Pattern.compile("\\s*\\$unpaid\\s/u/\\S+");

	private Logger logger;

	public UnpaidSummon() {
		logger = LogManager.getLogger();
	}
	
	
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = UNPAID_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String group = matcher.group().trim();
			String[] split = group.split("\\s");
			
			String doer = comment.author();
			String doneTo = BotUtils.getUser(split[1]);
			

			LoansDatabase database = (LoansDatabase) db;
			if(config.getList("banned").contains(doneTo.toLowerCase())) {
				logger.info("Someone is attempting to $unpaid a banned user");
				return new SummonResponse(SummonResponse.ResponseType.INVALID, config.getString("action_to_banned"));
			}
			
			User doneToU = database.getUserByUsername(doneTo);
			User doerU = database.getUserByUsername(doer);

			List<Loan> relevantLoans = (doneToU != null && doerU != null) ? database.getLoansWithBorrowerAndOrLender(doneToU.id, doerU.id, true) : new ArrayList<Loan>();
			List<Loan> changed = new ArrayList<>();

			for(Loan l : relevantLoans) {
				if(l.principalRepaymentCents != l.principalCents) {
					database.setLoanUnpaid(l, true);
					changed.add(l);
				}
			}

			logger.printf(Level.INFO, "%s has defaulted on %d loans from %s", doneTo, changed.size(), doer);

			String response = config.getString("unpaid").replace("<lender>", doer).replace("<borrower>", doneTo).replace("<loans>", LoansBotUtils.getLoansAsTable(changed, database, changed.size()));
			return new SummonResponse(SummonResponse.ResponseType.VALID, response);
		}
		return null;
	}

}
