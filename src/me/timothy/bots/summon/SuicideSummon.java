/**
 * 
 */
package me.timothy.bots.summon;

import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
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
	
	private SummonResponse getReply(FileConfiguration config, Database db, String user) {
		LoansDatabase database = (LoansDatabase) db;
		return new SummonResponse(SummonResponse.ResponseType.VALID, database.getResponseMapping().fetchByName("suicide").responseBody.replace("<user>", user));
	}

	@Override
	public boolean mightInteractWith(Comment comment, Database db, FileConfiguration config) {
		return PATTERN.matcher(comment.body()).find();
	}

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		return getReply(config, db, comment.author());
	}

	@Override
	public boolean mightInteractWith(Link link, Database db, FileConfiguration config) {
		return PATTERN.matcher(link.title()).find() || PATTERN.matcher(link.selftext()).find();
	}
	
	@Override
	public SummonResponse handleLink(Link link, Database db, FileConfiguration config) {
		return getReply(config, db, link.author());
	}


}
