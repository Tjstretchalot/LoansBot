package me.timothy.bots.summon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.User;
import me.timothy.bots.responses.GenericFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
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
	
	/**
	 * The format that the check summon expects
	 */
	public static final String CHECK_FORMAT = "$check <user1>";

	private Logger logger;

	public CheckSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public boolean mightInteractWith(Link link, Database db, FileConfiguration config) {
		String title = link.title();
		return !title.toUpperCase().startsWith("[META]");
	}
	
	@Override
	public SummonResponse handleLink(Link submission, Database db, FileConfiguration config) {
		LoansDatabase database = (LoansDatabase) db;

		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		String checked = submission.author();
		User checkedUser = database.getUserMapping().fetchOrCreateByName(checked);
		if(checkedUser == null) {
			respInfo.addTemporaryString("user1 id", Integer.toString(-1));
		}else {
			respInfo.addTemporaryString("user1 id", Integer.toString(checkedUser.id));
		}
		
		
		respInfo.addTemporaryObject("author", new GenericFormattableObject(checked));
		respInfo.addTemporaryObject("user1", new GenericFormattableObject(checked));
		
		ResponseFormatter formatter = new ResponseFormatter(database.getResponseMapping().fetchByName("check").responseBody, respInfo);
		logger.printf(Level.DEBUG, "%s posted a non-meta submission and recieved a check", respInfo.getObject("author").toString());
		return new SummonResponse(SummonResponse.ResponseType.VALID, formatter.getFormattedResponse(config, (LoansDatabase) db));
	}

	@Override
	public boolean mightInteractWith(Comment comment, Database db, FileConfiguration config) {
		return CHECK_PATTERN.matcher(comment.body()).find();
	}
	
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(comment.author().equalsIgnoreCase(config.getProperty("user.username"))) {
			return null;
		}
		
		Matcher matcher = CHECK_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			LoansDatabase database = (LoansDatabase) db;
			
			String text = matcher.group().trim();
			String author = comment.author();
			ResponseInfo respInfo = ResponseInfoFactory.getResponseInfo(CHECK_FORMAT, text, comment);
			String checked = respInfo.getObject("user1").toString();
			logger.printf(Level.INFO, "%s requested a check on %s", author, checked);

			User checkedUser = database.getUserMapping().fetchOrCreateByName(checked);
			if(checkedUser == null) {
				respInfo.addTemporaryString("user1 id", Integer.toString(-1));
			}else {
				respInfo.addTemporaryString("user1 id", Integer.toString(checkedUser.id));
			}
			
			ResponseFormatter respFormatter = new ResponseFormatter(database.getResponseMapping().fetchByName("check").responseBody, respInfo);
			return new SummonResponse(SummonResponse.ResponseType.VALID, respFormatter.getFormattedResponse(config, database));
		}
		
		return null;
	}


}
