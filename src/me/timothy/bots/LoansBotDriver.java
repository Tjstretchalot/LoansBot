package me.timothy.bots;

import me.timothy.bots.summon.Summon;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;
import me.timothy.jreddit.info.Thing;

/**
 * The bot driver for the loans bot
 * 
 * @author Timothy
 */
public class LoansBotDriver extends BotDriver {

	/**
	 * Exact echo of BotDriver constructor 
	 * @param database database
	 * @param config config
	 * @param bot the bot
	 * @param commentSummons comment summons
	 * @param pmSummons pm summons
	 * @param submissionSummons submission summons
	 */
	public LoansBotDriver(Database database, FileConfiguration config, Bot bot,
			Summon[] commentSummons, Summon[] pmSummons,
			Summon[] submissionSummons) {
		super(database, config, bot, commentSummons, pmSummons, submissionSummons);
	}

	/* (non-Javadoc)
	 * @see me.timothy.bots.BotDriver#handleReply(me.timothy.jreddit.info.Thing, java.lang.String)
	 */
	@Override
	protected void handleReply(Thing replyable, String response) {
		String subreddit = null;
		if(replyable instanceof Comment) {
			subreddit = ((Comment) replyable).subreddit();
		}else if(replyable instanceof Link) {
			subreddit = ((Link) replyable).subreddit();
		}
		
		if(subreddit != null) {
			if(LoansBotUtils.SECONDARY_SUBREDDITS.contains(subreddit.toLowerCase())) {
				if(!response.endsWith("\n\n")) {
					if(response.endsWith("\n")) {
						response = response + "\n";
					}else {
						response = response + "\n\n";
					}
					
					String postfix = ((LoansFileConfiguration) config).getSecondarySubredditPostfix();
					postfix = postfix.replace("<subreddit>", subreddit);
					postfix = postfix.replace("<primary>", LoansBotUtils.PRIMARY_SUBREDDIT);
					
					response = response + postfix;
				}
			}
		}
		super.handleReply(replyable, response);
	}

	
}
