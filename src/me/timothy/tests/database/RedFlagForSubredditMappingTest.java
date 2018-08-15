package me.timothy.tests.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.RedFlagForSubreddit;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class RedFlagForSubredditMappingTest {
	protected MappingDatabase database;

	@Test
	public void testTest() {
		assertNotNull(database);
	}

	@Test
	public void testAll() {
		final long now = System.currentTimeMillis();
		
		RedFlagForSubreddit suba = new RedFlagForSubreddit(-1, "suba", "who likes the as", new Timestamp(now));
		database.getRedFlagForSubredditMapping().save(suba);
		
		assertNotEquals(-1, suba.id);
		
		RedFlagForSubreddit fromDb = database.getRedFlagForSubredditMapping().fetchBySubreddit("suba");
		assertEquals(suba, fromDb);
		
		RedFlagForSubreddit subb = new RedFlagForSubreddit(-1, "subb", "too close to suba", new Timestamp(now));
		database.getRedFlagForSubredditMapping().save(subb);
		
		assertNotEquals(-1, subb.id);
		assertNotEquals(suba.id, subb.id);
		
		MysqlTestUtils.assertListContents(database.getRedFlagForSubredditMapping().fetchAll(), suba, subb);
		
		fromDb = database.getRedFlagForSubredditMapping().fetchBySubreddit("subb");
		assertEquals(subb, fromDb);
		
		fromDb = database.getRedFlagForSubredditMapping().fetchBySubreddit("suba");
		assertEquals(suba, fromDb);
	}
}
