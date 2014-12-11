/**
 * 
 */
package me.timothy.bots.summon;

import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;

/**
 * Called when the word suicide is in a comment or submission
 * 
 * @author Timothy
 */
public class SuicideSummon implements LinkSummon, CommentSummon {
	/**
	 * The pattern to search the messages for
	 */
	private static final Pattern PATTERN = Pattern.compile("suicide");
	
	private String user;

	private SummonResponse getReply(FileConfiguration config, Database database) {
		return new SummonResponse(SummonResponse.ResponseType.VALID, config.getString("suicide").replace("<user>", user));
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		user = comment.author();
		if(PATTERN.matcher(comment.body()).find())
			return getReply(config, db);
		return null;
	}

	@Override
	public SummonResponse handleLink(Link link, Database db, FileConfiguration config) {
		user = link.author();
		if(PATTERN.matcher(link.title()).find() || PATTERN.matcher(link.selftext()).find())
			return getReply(config, db);
		return null;
	}

}
