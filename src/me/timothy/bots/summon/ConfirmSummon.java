package me.timothy.bots.summon;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
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
	 * $unpaid /u/John $unpaid /u/Asdf_Jkl
	 */
	private static final Pattern CONFIRM_PATTERN = Pattern
			.compile("\\s*\\$confirm\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?");

	private Logger logger;

	public ConfirmSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = CONFIRM_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String text = matcher.group().trim();
			
			String doer = comment.author();
			String[] split = text.split("\\s");
			String doneTo = BotUtils.getUser(split[1]);
			String number = split[2].replace("$", "");

			int amountPennies;
			try {
				amountPennies = BotUtils.getPennies(number);
			} catch (ParseException e) {
				logger.warn(e);
				return null;
			}
			
			if(config.getList("banned").contains(doneTo.toLowerCase())) {
				logger.info("Someone is attempting to $confirm a banned user");
				return new SummonResponse(SummonResponse.ResponseType.INVALID, config.getString("action_to_banned"));
			}
			logger.printf(Level.INFO, "%s confirmed a $%s transfer from %s", doer, BotUtils.getCostString(amountPennies /100.), doneTo);
			
			String resp = config.getString("confirm").replace("<borrower>", doer).replace("<lender>", doneTo).replace("<amount>", BotUtils.getCostString(amountPennies / 100.));
			return new SummonResponse(SummonResponse.ResponseType.VALID, resp);
		}
		return null;
	}

}
