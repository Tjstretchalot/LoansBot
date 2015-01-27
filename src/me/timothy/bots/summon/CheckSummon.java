package me.timothy.bots.summon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.responses.GenericFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
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
	
	/**
	 * The format that the check summon expects
	 */
	public static final String CHECK_FORMAT = "$check <user1>";

	private Logger logger;

	public CheckSummon() {
		logger = LogManager.getLogger();
	}
	
	@Override
	public SummonResponse handleLink(Link submission, Database db, FileConfiguration config) {
		String title = submission.title();
		if(title.toUpperCase().startsWith("[META]"))
			return null;
		

		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		respInfo.addTemporaryObject("author", new GenericFormattableObject(submission.author()));
		respInfo.addTemporaryObject("user1", new GenericFormattableObject(submission.author()));
		ResponseFormatter formatter = new ResponseFormatter(config.getString("check"), respInfo);
		logger.printf(Level.DEBUG, "%s posted a non-meta submission and recieved a check", respInfo.getObject("author").toString());
		return new SummonResponse(SummonResponse.ResponseType.VALID, formatter.getFormattedResponse(config, (LoansDatabase) db));
	}
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		Matcher matcher = CHECK_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			String text = matcher.group().trim();
			String author = comment.author();
			ResponseInfo respInfo = ResponseInfoFactory.getResponseInfo(CHECK_FORMAT, text);
			respInfo.addTemporaryObject("author", new GenericFormattableObject(author));
			logger.printf(Level.INFO, "%s requested a check on %s", author, respInfo.getObject("user1").toString());
			ResponseFormatter respFormatter = new ResponseFormatter(config.getString("check"), respInfo);
			return new SummonResponse(SummonResponse.ResponseType.VALID, respFormatter.getFormattedResponse(config, (LoansDatabase) db));
		}
		
		return null;
	}

}
