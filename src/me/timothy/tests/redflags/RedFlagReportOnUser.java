package me.timothy.tests.redflags;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.Bot;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.RedFlagForSubreddit;
import me.timothy.bots.models.RedFlagQueueSpot;
import me.timothy.bots.models.User;
import me.timothy.bots.redflags.RedFlagsDriver;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class RedFlagReportOnUser {
	private LoansDatabase database;
	private RedFlagsDriver driver;
	private Bot loansBot;
	private Logger logger;
	
	@Before
	public void setUp() throws NullPointerException, IOException, ParseException {
		Properties testDBProperties = MysqlTestUtils.fetchTestDatabaseProperties();
		LoansDatabase testDb = MysqlTestUtils.getDatabase(testDBProperties);
		MysqlTestUtils.clearDatabase(testDb);
		
		database = testDb;
		
		LoansFileConfiguration config = new LoansFileConfiguration();
		config.setFolder(Paths.get("tests"));
		config.load();

		loansBot = new Bot("borrow"); 
		loansBot.loginReddit(config.getProperty("user.username"),
				config.getProperty("user.password"),
				config.getProperty("user.appClientID"),
				config.getProperty("user.appClientSecret"));
		
		logger = LogManager.getLogger();
		driver = new RedFlagsDriver(database, config, loansBot, new Runnable() {
			@Override
			public void run() {
			}
		}, 2000);
		
		
		database.getRedFlagForSubredditMapping().save(new RedFlagForSubreddit(-1, "UniversalScammerList", "testing", new Timestamp(System.currentTimeMillis())));
	}
	
	@Test
	public void fetchUser() throws IOException, ParseException, java.text.ParseException {
		User user = database.getUserMapping().fetchOrCreateByName("tjstretchalot");
		
		driver.enqueue(user.id);

		RedFlagQueueSpot spot = database.getRedFlagQueueSpotMapping().fetchOldestUncompleted();
		while(spot != null) {
			driver.handleQueue(5);
			spot = database.getRedFlagQueueSpotMapping().fetchOldestUncompleted();
		}
	}

	@After 
	public void cleanUp() {
		logger.debug("finished");
		((LoansDatabase) database).disconnect();
	}
}
