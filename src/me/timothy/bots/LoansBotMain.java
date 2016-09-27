package me.timothy.bots;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.currencies.CurrencyHandler;
import me.timothy.bots.summon.BadLoanSummon;
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

/**
 * Entry point to the program. Handles initializing the bot on a local
 * level (loading configuration, connecting to the database, etc) and
 * begins the bot driver.
 * 
 * @author Timothy
 */
public class LoansBotMain {
	public static void main(String[] args) {
		LoansResponseInfoFactory.init();
		
		Logger logger = LogManager.getLogger();
		
		
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
		CurrencyHandler.getInstance().accessCode = config.getProperty("currencylayer.access_code");
		Utils.USER_AGENT = config.getProperty("user.appClientID") + ":v09.26.2016 (by /u/Tjstretchalot)";
		
		logger.debug("Connecting to database..");
		LoansDatabase database = new LoansDatabase();
		
		try {
			database.connect(config.getProperty("database.username"), config.getProperty("database.password"), config.getProperty("database.url"));
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		logger.debug("Verifying database schema..");
		database.validateTableState();
		
		logger.debug("Running loans bot driver");
		BotDriver driver = new LoansBotDriver(database, config, loansBot,
				new CommentSummon[] { new CheckSummon(), new LoanSummon(), new PaidSummon(), new ConfirmSummon(), new UnpaidSummon(), new SuicideSummon(), new BadLoanSummon() }, 
				new PMSummon[] { },
				new LinkSummon[] { new CheckSummon(), new SuicideSummon() });
		
		while(true) {
			try {
				driver.run();
			}catch(Exception e) {
				e.printStackTrace();
				logger.log(Level.FATAL, e.getMessage(), e);
				
				logger.catching(e);
				driver.sleepFor(2000);
			}
		}
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
