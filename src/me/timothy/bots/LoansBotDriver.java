package me.timothy.bots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.timothy.bots.emailsummon.EmailSummon;
import me.timothy.bots.summon.CommentSummon;
import me.timothy.bots.summon.LinkSummon;
import me.timothy.bots.summon.PMSummon;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;
import me.timothy.jreddit.info.Thing;

import org.json.simple.parser.ParseException;

/**
 * The bot driver for the loans bot
 * 
 * @author Timothy
 */
public class LoansBotDriver extends BotDriver {
	/**
	 * Email summons
	 */
	private EmailSummon[] emailSummons;
	
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
			CommentSummon[] commentSummons, PMSummon[] pmSummons,
			LinkSummon[] submissionSummons, EmailSummon[] emailSummons) {
		super(database, config, bot, commentSummons, pmSummons, submissionSummons);
		
		this.emailSummons = emailSummons;
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

		logger.trace("Checking for pending applicants..");
		checkPendingApplicants();
		
		logger.trace("Checking for any emails..");
		checkEmails();
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
				logger.info(a.getUsername() + "'s application was denied (duplicate information)");
				si.sendEmail(a.getEmail(), a.getFirstName(), "Application Denied", "Your application to /r/Borrow was denied:\n\n- Duplicate Information");
				sleepFor(2000);
				continue;
			}

			logger.info(a.getUsername() + "'s application will be accepted if the email works");
			if(si.sendEmail(a.getEmail(), a.getFirstName(), "Application Accepted", "Your application to /r/Borrow was accepted, please read the sidebar before posting"))
				ldb.addApplicant(a);
			else {
				logger.info(a.getUsername() + "'s application was denied (invalid email)");
			}
			sleepFor(2000);
		}

		for(int i = 0; i < pendingApplicants.size(); i++) {
			logger.info("Removing " + pendingApplicants.get(i).getUsername() + " from spreadsheet");
			si.removeTopApplicant();
			sleepFor(1000);
		}
	}
	
	/**
	 * Checks if there are any new, unread emails and 
	 * also checks if that matches any EmailSummons. If it
	 * does, handles that appropriately
	 */
	protected void checkEmails() {
		
	}

	/**
	 * Sends a message to the specified user with the specified
	 * title & message
	 * @param user the user to send the message to
	 * @param title the title of the message
	 * @param message the text of the message
	 */
	@SuppressWarnings("unused")
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
