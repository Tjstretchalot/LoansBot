package me.timothy.tests.bots.summon;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.json.simple.JSONObject;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.LoansResponseInfoFactory;
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
}
