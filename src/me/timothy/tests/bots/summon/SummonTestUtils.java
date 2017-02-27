package me.timothy.tests.bots.summon;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Properties;

import org.json.simple.JSONObject;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.LoansResponseInfoFactory;
import me.timothy.bots.currencies.CurrencyHandler;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;
import me.timothy.tests.database.mysql.MysqlTestUtils;

/**
 * Contains a collection of utility functions for tests
 * regarding summons
 * 
 * @author Timothy
 */
public class SummonTestUtils {

	/**
	 * Creates a link that has the specified title and author. Everything else
	 * will throw a null pointer exception
	 * 
	 * @param title the title of the link
	 * @param author the author of the link
 	 * @return the link
	 */
	@SuppressWarnings("unchecked")
	public static Link createLinkByTitleAndAuthor(String title, String author) {
		JSONObject obj = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("title", title);
		data.put("author", author);
		obj.put("data", data);
		return new Link(obj);
	}

	/**
	 * Creates a link that has the specified title, author, subreddit, and time. Everything else
	 * will throw a null pointer exception
	 * 
	 * @param title the title of the link
	 * @param author the author of the link
	 * @param subreddit the subreddit of the link
	 * @param timestamp the created timestamp
 	 * @return the link
	 */
	@SuppressWarnings("unchecked")
	public static Link createLinkByTitleAndAuthorAndSubAndTime(String title, String author, String subreddit, long timestamp) {
		JSONObject obj = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("title", title);
		data.put("author", author);
		data.put("subreddit", subreddit);
		data.put("created_utc", timestamp / 1000.0);
		obj.put("data", data);
		return new Link(obj);
	}
	
	/**
	 * Creates a link that has the specified title as it's title. Everything
	 * else will throw null pointer exception.
	 * 
	 * @param title the title
	 * @return the link
	 */
	public static Link createLinkByTitle(String title) {
		return createLinkByTitleAndAuthor(title, null);
	}
	
	/**
	 * Creates a comment that has the specified body and author. Sets the
	 * created time to the current time. Everything else will throw a null 
	 * pointer exception.
	 * 
	 * @param body the comment body
	 * @param author the comment author
	 * @return the comment
	 */
	@SuppressWarnings("unchecked")
	public static Comment createComment(String body, String author) {
		JSONObject obj = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("body", body);
		data.put("author", author);
		data.put("created_utc", System.currentTimeMillis() / 1000.);
		obj.put("data", data);
		return new Comment(obj);
	}

	/**
	 * Creates a comment that has the specified body, author, and link author.
	 * Sets the created time to the current time. The link is set to http://reddit.com.
	 * Everything else will return null or a null pointer exception.
	 * 
	 * @param body the comment body
	 * @param author the comment author
	 * @param linkAuthor the author of the link the comment was posted under
	 * @return the comment
	 */
	@SuppressWarnings("unchecked")
	public static Comment createComment(String body, String author, String linkAuthor) {
		JSONObject obj = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("body", body);
		data.put("author", author);
		data.put("link_author", linkAuthor);
		data.put("created_utc", System.currentTimeMillis() / 1000.);
		data.put("link_url", "http://reddit.com");
		obj.put("data", data);
		return new Comment(obj);
	}
	
	/**
	 * Fetches the test database, already connected to and initialized
	 * 
	 * @return the test database
	 */
	public static LoansDatabase getTestDatabase() {
		Properties testDBProperties = MysqlTestUtils.fetchTestDatabaseProperties();
		LoansDatabase testDb = MysqlTestUtils.getDatabase(testDBProperties);
		MysqlTestUtils.clearDatabase(testDb);
		return testDb;
	}
	
	/**
	 * Gets the configuration used for testing.
	 * @return the configuration
	 * @throws IOException if a file is saved incorrectly / locked
	 * @throws NullPointerException if a file or key that was expected couldn't be found
	 */
	public static LoansFileConfiguration getTestConfig() throws NullPointerException, IOException {
		LoansResponseInfoFactory.init();
		LoansFileConfiguration config = new LoansFileConfiguration();
		config.setFolder(Paths.get("tests"));
		config.load();
		return config;
	}
	
	/**
	 * Overrides the currency conversion to force it to return only the specified conversion factor
	 * for the specified conversion, and error on all other requested conversions.
	 * 
	 * @param conversionFrom the from currency
	 * @param conversionTo the to currency
	 * @param conversionFactor the conversion factor
	 * @throws SecurityException if one occurs
	 * @throws NoSuchFieldException if one occurs
	 * @throws IllegalAccessException if one occurs
	 * @throws IllegalArgumentException if one occurs
	 */
	public static void overrideCurrencyConversion(final String conversionFrom, final String conversionTo, final double conversionFactor) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<CurrencyHandler> chCl = CurrencyHandler.class;
		Field instance = chCl.getDeclaredField("instance");
		instance.setAccessible(true);
		instance.set(null, new CurrencyHandler() {

			@Override
			public double getConversionRate(String from, String to) {
				if(!from.equalsIgnoreCase(conversionFrom) || !to.equalsIgnoreCase(conversionTo)) {
					throw new IllegalArgumentException(
							String.format("Weird conversion requested during test: %s -> %s (%s -> %s expected)",
									from, to, conversionFrom, conversionTo));
				}
				
				return conversionFactor;
			}
			
		});
	}
}
