package me.timothy.bots.redflags;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagForSubreddit;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.Username;

/**
 * Adds a red flag for subreddits in the RedFlagForSubreddit table
 * 
 * @author Timothy
 */
public class RedFlagForSubredditDetector implements IRedFlagDetector {
	private LoansDatabase database;

	public RedFlagForSubredditDetector(LoansDatabase db, LoansFileConfiguration config) {
		database = db;
	}

	@Override
	public void start(Username username) {
	}

	private List<RedFlag> redFlagForSubreddit(String subreddit) {
		RedFlagForSubreddit redFlag = database.getRedFlagForSubredditMapping().fetchBySubreddit(subreddit);

		if(redFlag == null)
			return Collections.emptyList();

		return Collections.singletonList(new RedFlag(-1, 1, RedFlag.RedFlagType.SUBREDDIT, redFlag.subreddit, redFlag.description, 1, new Timestamp(System.currentTimeMillis())));
	}

	@Override
	public List<RedFlag> parseComment(RedFlagUserHistoryComment comment) {
		return redFlagForSubreddit(comment.subreddit);
	}

	@Override
	public List<RedFlag> parseLink(RedFlagUserHistoryLink link) {
		return redFlagForSubreddit(link.subreddit);
	}

	@Override
	public List<RedFlag> finish() {
		return Collections.emptyList();
	}

	@Override
	public boolean requiresResweep() {
		return false;
	}
}
