package me.timothy.tests.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.database.RedFlagUserHistoryCommentMapping;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class RedFlagUserHistoryCommentMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testAll() {
		RedFlagUserHistoryComment comment, comment2, fromDb;
		RedFlagUserHistoryCommentMapping mapping = database.getRedFlagUserHistoryCommentMapping();
		
		User john = database.getUserMapping().fetchOrCreateByName("john");
		Username johnUsername = database.getUsernameMapping().fetchByUsername("john");
		
		RedFlagReport report = new RedFlagReport(-1, johnUsername.id, "asdf", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null);
		database.getRedFlagReportMapping().save(report);
		
		comment = new RedFlagUserHistoryComment(-1, report.id, john.id, "t1_comment", "http://www.site.com/link", "the body of the comment", "subreddit", new Timestamp(System.currentTimeMillis() - 10000), null);
		mapping.save(comment);
		
		assertNotEquals(-1, comment.id);
		assertTrue(comment.id > 0);
		
		fromDb = mapping.fetchByID(comment.id);
		assertEquals(comment, fromDb);
		
		MysqlTestUtils.assertListContents(mapping.fetchAll(), comment);
		
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		Username paulUsername = database.getUsernameMapping().fetchByUsername("paul");
		
		RedFlagReport report2 = new RedFlagReport(-1, paulUsername.id, "fdsa", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null);
		database.getRedFlagReportMapping().save(report2);
		
		comment2 = new RedFlagUserHistoryComment(-1, report2.id, paul.id, "t1_comment2", "link", "body", "sub", new Timestamp(System.currentTimeMillis() - 10000), new Timestamp(System.currentTimeMillis() - 5000));
		mapping.save(comment2);
		
		assertNotEquals(-1, comment2.id);
		assertNotEquals(comment.id, comment2.id);
		assertNotEquals(comment, comment2);
		
		fromDb = mapping.fetchByID(comment.id);
		assertEquals(comment, fromDb);
		
		fromDb = mapping.fetchByID(comment2.id);
		assertEquals(comment2, fromDb);
		
		MysqlTestUtils.assertListContents(mapping.fetchAll(), comment, comment2);
		
		mapping.deleteByReportID(report.id);
		
		MysqlTestUtils.assertListContents(mapping.fetchAll(), comment2);
		fromDb = mapping.fetchByID(comment.id);
		assertNull(fromDb);
		
		mapping.deleteByReportID(report2.id);
		MysqlTestUtils.assertListContents(mapping.fetchAll());
	}
}
