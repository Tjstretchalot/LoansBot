package me.timothy.tests.database;

import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.RecentPost;

/**
 * Describes a test focused on a RecentPostsMapping in a MappingDatabase. The database
 * must be <i>completely</i> empty after each setUp. The database <i>will</i> be
 * modified after each run - do not run against real databases.
 * 
 * @author Timothy
 */
public class RecentPostMappingTest {
	/**
	 * The mapping database that contains the RecentPostsMapping to be tested.
	 * Must be initialized by the child class.
	 */
	protected MappingDatabase database;
	

	/**
	 * Tests to see that the test is setup correctly by verifying that the
	 * {@link #database database} is not null.
	 */
	@Test
	public void testTest() {
		assertNotNull(database);
	}

	/**
	 * Tests that {@link me.timothy.bots.database.ObjectMapping#save(Object) saving}
	 * recent posts will set their {@link RecentPost#id id} to a strictly positive
	 * number, and that the recent post can be fetched again with
	 * {@link me.timothy.bots.database.ObjectMapping#fetchAll() fetchAll()}
	 */
	@Test
	public void testSave() {
		final String author = "johndoe";
		final String subreddit = "somerandomsub";
		RecentPost recentPost = new RecentPost(-1, author, subreddit, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		
		database.getRecentPostMapping().save(recentPost);
		
		assertTrue(recentPost.id > 0);
		assertTrue(recentPost.author.equals(author));
		assertTrue(recentPost.subreddit.equals(subreddit));
		
		List<RecentPost> fromDb = database.getRecentPostMapping().fetchAll();
		assertListContents(fromDb, recentPost);
	}
	
	/**
	 * Tests that once {@link me.timothy.bots.database.ObjectMapping#save(Object) saved} recent posts
	 * can be {@link me.timothy.bots.database.RecentPostMapping#fetchByUsername(String) found by username}
	 */
	@Test
	public void testFetchByUsername() {
		final String author = "johndoe";
		final String notauthor = "smithconley";
		final String subreddit = "somerandomsub";
		final long firstTime = System.currentTimeMillis();
		final long secondTime = firstTime - 1000 * 60 * 60 * 12;
		
		RecentPost recentPost1 = new RecentPost(-1, author, subreddit, new Timestamp(firstTime), new Timestamp(firstTime));
		database.getRecentPostMapping().save(recentPost1);
		
		assertTrue(recentPost1.id > 0);
		
		List<RecentPost> fromDb = database.getRecentPostMapping().fetchByUsername(author);
		assertListContents(fromDb, recentPost1);
		
		fromDb = database.getRecentPostMapping().fetchByUsername(notauthor);
		assertListContents(fromDb);
		
		RecentPost recentPost2 = new RecentPost(-1, author, subreddit, new Timestamp(secondTime), new Timestamp(secondTime));
		database.getRecentPostMapping().save(recentPost2);
		
		assertTrue(recentPost2.id > 0);
		assertTrue(recentPost1.id != recentPost2.id);
		
		fromDb = database.getRecentPostMapping().fetchByUsername(author);
		assertListContents(fromDb, recentPost1, recentPost2);
	}
	
	/**
	 * Tests that {@link me.timothy.bots.database.RecentPostMapping#deleteOldEntries() deleteOldEntries()} deletes
	 * entries older than 1 week
	 */
	@Test
	public void testPruneOldEntries()
	{
		final String author = "holysmith";
		final String subreddit = "reallycoolsub";
		final long oldEnoughTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 10; // 10 days ago
		final long notOldEnoughTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24; // yesterday
		
		RecentPost recentPost1 = new RecentPost(-1, author, subreddit, new Timestamp(oldEnoughTime), new Timestamp(oldEnoughTime));
		database.getRecentPostMapping().save(recentPost1);
		
		assertTrue(recentPost1.id > 0);
		
		List<RecentPost> fromDb = database.getRecentPostMapping().fetchAll();
		assertListContents(fromDb, recentPost1);
		
		RecentPost recentPost2 = new RecentPost(-1, author, subreddit, new Timestamp(notOldEnoughTime), new Timestamp(notOldEnoughTime));
		database.getRecentPostMapping().save(recentPost2);
		
		assertTrue(recentPost2.id > 0);
		
		fromDb = database.getRecentPostMapping().fetchAll();
		assertListContents(fromDb, recentPost1, recentPost2);
		
		database.getRecentPostMapping().deleteOldEntries();
		fromDb = database.getRecentPostMapping().fetchAll();
		assertListContents(fromDb, recentPost2);
	}
}
