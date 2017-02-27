package me.timothy.tests.bots.summon;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.RecentPost;
import me.timothy.bots.models.Response;
import me.timothy.bots.summon.RecentPostSummon;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.bots.summon.SummonResponse.ResponseType;
import me.timothy.jreddit.info.Link;

public class RecentPostSummonTests {
	private RecentPostSummon summon;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	
	@Before 
	public void setUp() throws Exception {
		summon = new RecentPostSummon();
		database = SummonTestUtils.getTestDatabase();
		config = SummonTestUtils.getTestConfig();
	}

	@After
	public void tearDown() {
		database.disconnect();
		database = null;
	}
	
	/**
	 * Test that summon is not null (@Before is working correctly)
	 */
	@Test
	public void testTest()
	{
		assertNotNull(summon);
	}
	
	@Test
	public void testAddsToRecentPosts()
	{
		final long now = (System.currentTimeMillis() / 1000) * 1000;
		final String author = "johndoe";
		final String subreddit = "eallyinteresting";
		List<RecentPost> fromDb = database.getRecentPostMapping().fetchAll();
		assertListContents(fromDb);
		
		Link link = SummonTestUtils.createLinkByTitleAndAuthorAndSubAndTime("[REQ] hello friends", author, subreddit, now);
		
		SummonResponse response = summon.handleLink(link, database, config);
		assertNull(response);
		
		fromDb = database.getRecentPostMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertEquals(author, fromDb.get(0).author);
		assertEquals(subreddit, fromDb.get(0).subreddit);
		assertEquals(now, fromDb.get(0).createdAt.getTime());
		assertEquals(now, fromDb.get(0).updatedAt.getTime());
	}
	
	@Test
	public void testReturnsReportWhenAppropriate()
	{
		final long recent = System.currentTimeMillis() - 60000;
		final long also_recent = System.currentTimeMillis();
		final String author = "johndoe";
		final String subreddit = "eallyinteresting";
		
		Link link = SummonTestUtils.createLinkByTitleAndAuthorAndSubAndTime("[REQ] hello friends", author, subreddit, recent);
		
		SummonResponse response = summon.handleLink(link, database, config);
		assertNull(response);
		
		link = SummonTestUtils.createLinkByTitleAndAuthorAndSubAndTime("[REQ] guess whos back", author, subreddit, also_recent);
		
		Response resp = new Response();
		resp.name = "too_recent_request_report";
		resp.responseBody = "this guy (facepalm)";
		resp.id = -1;
		resp.createdAt = new Timestamp(recent);
		resp.updatedAt = new Timestamp(recent);
		database.getResponseMapping().save(resp);
		
		response = summon.handleLink(link, database, config);
		assertNotNull(response);
		assertEquals(resp.responseBody, response.getReportMessage());
		assertEquals(ResponseType.SILENT, response.getResponseType());
	}
	
	@Test
	public void testDoesntReportOkayPosts()
	{
		final long not_recent = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2;
		final long recent = System.currentTimeMillis();
		final String author = "johndoe";
		final String subreddit = "eallyinteresting";
		
		Link link = SummonTestUtils.createLinkByTitleAndAuthorAndSubAndTime("[REQ] hello friends", author, subreddit, not_recent);
		
		SummonResponse response = summon.handleLink(link, database, config);
		assertNull(response);
		
		link = SummonTestUtils.createLinkByTitleAndAuthorAndSubAndTime("[REQ] guess whos back", author, subreddit, recent);
		
		response = summon.handleLink(link, database, config);
		assertNull(response);
	}
}
