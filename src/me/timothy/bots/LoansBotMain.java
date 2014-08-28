package me.timothy.bots;

import java.io.IOException;
import java.sql.SQLException;

import me.timothy.bots.summon.AdvancedLoanSummon;
import me.timothy.bots.summon.CheckSummon;
import me.timothy.bots.summon.ConfirmSummon;
import me.timothy.bots.summon.LoanSummon;
import me.timothy.bots.summon.PaidSummon;
import me.timothy.bots.summon.Summon;
import me.timothy.bots.summon.UnpaidSummon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

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
		
		logger.debug("Loading rest client..");
		RestClient restClient = new HttpRestClient();
		restClient.setUserAgent("LoansBot by /u/Tjstretchalot");
		
		logger.debug("Initializing loans bot..");
		Bot loansBot = new Bot("Loans");
		loansBot.setRestClient(restClient);
		
		logger.debug("Loading config..");
		FileConfiguration config = new FileConfiguration();
		try {
			config.load();
		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		logger.debug("Connecting to database..");
		LoansDatabase database = new LoansDatabase();
		
		try {
			database.connect(config.getDatabaseInfo().getProperty("username"), config.getDatabaseInfo().getProperty("password"), config.getDatabaseInfo().getProperty("url"));
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		logger.debug("Running loans bot driver");
		BotDriver driver = new BotDriver(database, config, restClient, loansBot,
				new Summon[] { new CheckSummon(), new LoanSummon(), new PaidSummon(), new ConfirmSummon(), new UnpaidSummon() }, 
				new Summon[] { new AdvancedLoanSummon(), new PaidSummon(), new UnpaidSummon() },
				new Summon[] { new CheckSummon() } );
		
		driver.run();
	}
}
