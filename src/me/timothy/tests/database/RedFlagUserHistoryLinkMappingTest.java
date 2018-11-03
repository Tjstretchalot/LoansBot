package me.timothy.tests.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.database.RedFlagUserHistoryLinkMapping;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class RedFlagUserHistoryLinkMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testAll() {
		RedFlagUserHistoryLink link, link2, fromDb;
		RedFlagUserHistoryLinkMapping mapping = database.getRedFlagUserHistoryLinkMapping();
		
		User john = database.getUserMapping().fetchOrCreateByName("john");
		Username johnUsername = database.getUsernameMapping().fetchByUsername("john");
		
		RedFlagReport report = new RedFlagReport(-1, johnUsername.id, "asdf", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null);
		database.getRedFlagReportMapping().save(report);
		
		link = new RedFlagUserHistoryLink(-1, report.id, john.id, "t3_link", "title", "url", null, "permalink", "subreddit", new Timestamp(System.currentTimeMillis() - 10000), null);
		mapping.save(link);
		
		assertNotEquals(-1, link.id);
		assertTrue(link.id > 0);
		
		fromDb = mapping.fetchByID(link.id);
		assertEquals(link, fromDb);
		
		link.permalink = "new_permalink";
		assertNotEquals(fromDb, link);
		
		mapping.save(link);
		fromDb = mapping.fetchByID(link.id);
		assertEquals(fromDb, link);
		
		MysqlTestUtils.assertListContents(mapping.fetchAll(), link);
		
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		Username paulUsername = database.getUsernameMapping().fetchByUsername("paul");
		
		RedFlagReport report2 = new RedFlagReport(-1, paulUsername.id, "fdsa", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null);
		database.getRedFlagReportMapping().save(report2);
		
		link2 = new RedFlagUserHistoryLink(-1, report2.id, paul.id, "t3_link2", "title2", null, "body", "permalink", "subreddit", new Timestamp(System.currentTimeMillis() - 10000), new Timestamp(System.currentTimeMillis() - 5000));
		mapping.save(link2);
		
		assertNotEquals(-1, link2.id);
		assertNotEquals(link.id, link2.id);
		assertNotEquals(link, link2);
		
		fromDb = mapping.fetchByID(link.id);
		assertEquals(link, fromDb);
		
		fromDb = mapping.fetchByID(link2.id);
		assertEquals(link2, fromDb);
		
		MysqlTestUtils.assertListContents(mapping.fetchAll(), link, link2);
		
		mapping.deleteByReportID(report.id);
		
		MysqlTestUtils.assertListContents(mapping.fetchAll(), link2);
		fromDb = mapping.fetchByID(link.id);
		assertNull(fromDb);
		
		mapping.deleteByReportID(report2.id);
		MysqlTestUtils.assertListContents(mapping.fetchAll());
	}
}
