package me.timothy.bots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;

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

	/* (non-Javadoc)
	 * @see me.timothy.bots.BotDriver#doLoop()
	 */
	@Override
	protected void doLoop() throws IOException, ParseException,
			java.text.ParseException {
		super.doLoop();
		
		logger.debug("Checking for pending applicants..");
		checkPendingApplicants();
	}

	/**
	 * Check and handle any pending applicants 
	 * 
	 */
	protected void checkPendingApplicants() {
		LoansDatabase ldb = (LoansDatabase) database;
		
		SpreadsheetIntegration si = ldb.getSpreadsheetIntegration();
		
		List<Applicant> pendingApplicants = si.getPendingApplicants();
		
		if(pendingApplicants.size() == 0)
			return;
		
		logger.debug("There are " + pendingApplicants.size() + " pending applicants..");
		for(Applicant a : pendingApplicants) {
			List<Applicant> duplicates = new ArrayList<>();
			
			duplicates.addAll(ldb.getApplicantByUsername(a.getUsername()));
			duplicates.addAll(ldb.getApplicantsByInfo(a.getFirstName(), a.getLastName(), a.getStreetAddress(), a.getCity(), 
					a.getState(), a.getCountry()));
			
			if(duplicates.size() > 0) {
				sendMessage(a.getUsername(), "Application Denied", "Your application to /r/Borrow was denied:\n\n- Duplicate Information");
			}
			
		}
	}
	
	/**
	 * Sends a message to the specified user with the specified
	 * title & message
	 * @param user the user to send the message to
	 * @param title the title of the message
	 * @param message the text of the message
	 */
	private void sendMessage(final String to, final String title, final String message) {
		new Retryable<Boolean>("Send PM") {

			@Override
			protected Boolean runImpl() throws Exception {
				if(!bot.sendPM(to, title, message)) {
					logger.warn("Failed to send " + message + " to " + to);
				}
				return true;
			}
			
		}.run();
	}
}
