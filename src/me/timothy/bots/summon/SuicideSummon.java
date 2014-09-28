/**
 * 
 */
package me.timothy.bots.summon;

import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansFileConfiguration;
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
	
	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(me.timothy.jreddit.info.Link)
	 */
	@Override
	public boolean parse(Link submission) throws UnsupportedOperationException {
		user = submission.author();
		
		return PATTERN.matcher(submission.title()).find() || PATTERN.matcher(submission.selftext()).find();
	}

	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(me.timothy.jreddit.info.Comment)
	 */
	@Override
	public boolean parse(Comment comment) throws UnsupportedOperationException {
		user = comment.author();
		
		return PATTERN.matcher(comment.body()).find();
	}

	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#applyChanges(me.timothy.bots.FileConfiguration, me.timothy.bots.Database)
	 */
	@Override
	public String applyChanges(FileConfiguration config, Database database) {
		LoansFileConfiguration lcf = (LoansFileConfiguration) config;
		
		return lcf.getSuicide().replace("<user>", user);
	}

}
