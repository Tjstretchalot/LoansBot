package me.timothy.bots;

import java.io.IOException;
import java.sql.SQLException;

import me.timothy.bots.emailsummon.EmailSummon;
import me.timothy.bots.emailsummon.VerifySummon;
import me.timothy.bots.summon.AdvancedLoanSummon;
import me.timothy.bots.summon.CheckSummon;
import me.timothy.bots.summon.CommentSummon;
import me.timothy.bots.summon.ConfirmSummon;
import me.timothy.bots.summon.LinkSummon;
import me.timothy.bots.summon.LoanSummon;
import me.timothy.bots.summon.PMSummon;
import me.timothy.bots.summon.PaidSummon;
import me.timothy.bots.summon.SuicideSummon;
import me.timothy.bots.summon.UnpaidSummon;
import me.timothy.jreddit.requests.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point to the program. Handles initializing the bot on a local
 * level (loading configuration, connecting to the database, etc) and
 * begins the bot driver.
 * 
 * @author Timothy
 */
public class LoansBotMain {
	public static void main(String[] args) {
		Logger logger = LogManager.getLogger();
		
		Utils.USER_AGENT = "LoansBot by /u/Tjstretchalot";
		
		logger.debug("Initializing loans bot..");
		Bot loansBot = new Bot(getSubreddit());
		
		logger.debug("Loading config..");
		LoansFileConfiguration config = new LoansFileConfiguration();
		try {
			config.load();
		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		logger.debug("Connecting to Google..");
		SpreadsheetIntegration si = new SpreadsheetIntegration(config);
		
		logger.debug("Connecting to database..");
		LoansDatabase database = new LoansDatabase(si);
		
		try {
			database.connect(config.getDatabaseInfo().getProperty("username"), config.getDatabaseInfo().getProperty("password"), config.getDatabaseInfo().getProperty("url"));
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		logger.debug("Running loans bot driver");
		BotDriver driver = new LoansBotDriver(database, config, loansBot,
				new CommentSummon[] { new CheckSummon(), new LoanSummon(), new PaidSummon(), new ConfirmSummon(), new UnpaidSummon(), new SuicideSummon() }, 
				new PMSummon[] { new AdvancedLoanSummon(), new PaidSummon(), new UnpaidSummon() },
				new LinkSummon[] { new CheckSummon(), new SuicideSummon() },
				new EmailSummon[] { new VerifySummon() });
		
		driver.run();
	}

	/**
	 * Gets the subreddit string for the primary + 
	 * secondary subreddits
	 * 
	 * @return the subreddits like so: a+b+c
	 */
	private static String getSubreddit() {
		StringBuilder subredditBuilder = new StringBuilder(LoansBotUtils.PRIMARY_SUBREDDIT);
		for(String sub : LoansBotUtils.SECONDARY_SUBREDDITS) {
			subredditBuilder.append("+").append(sub);
		}
		return subredditBuilder.toString();
	}
}
