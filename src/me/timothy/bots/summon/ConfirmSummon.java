package me.timothy.bots.summon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.responses.GenericFormattableObject;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
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
	 * $confirm /u/John $10
	 */
	private static final Pattern CONFIRM_PATTERN = Pattern
			.compile("\\s*\\$confirm\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?");
	
	private static final String CONFIRM_FORMAT = "$confirm <user1> <money1>";

	private Logger logger;

	public ConfirmSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = CONFIRM_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String text = matcher.group().trim();
			ResponseInfo ri = ResponseInfoFactory.getResponseInfo(CONFIRM_FORMAT, text);
			ri.addTemporaryObject("author", new GenericFormattableObject(comment.author()));
			
			logger.printf(Level.INFO, "%s confirmed a $%s transfer from %s", ri.getObject("author").toString(),
					((MoneyFormattableObject) ri.getObject("money1")).getAmount(), ri.getObject("user1").toString());
			
			ResponseFormatter formatter = new ResponseFormatter(config.getString("confirm"), ri);
			
			return new SummonResponse(SummonResponse.ResponseType.VALID, formatter.getFormattedResponse(config, (LoansDatabase) db));
		}
		return null;
	}

}
