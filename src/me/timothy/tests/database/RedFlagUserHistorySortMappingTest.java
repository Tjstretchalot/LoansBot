package me.timothy.tests.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.database.RedFlagUserHistoryCommentMapping;
import me.timothy.bots.database.RedFlagUserHistoryLinkMapping;
import me.timothy.bots.database.RedFlagUserHistorySortMapping;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.RedFlagUserHistorySort;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class RedFlagUserHistorySortMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testAll() {
		long now = System.currentTimeMillis();
		long step = 2000;
		
		RedFlagUserHistoryComment comment, comment2;
		RedFlagUserHistoryLink link, link2;
		RedFlagUserHistoryCommentMapping cMapping = database.getRedFlagUserHistoryCommentMapping();
		RedFlagUserHistoryLinkMapping lMapping = database.getRedFlagUserHistoryLinkMapping();
		RedFlagUserHistorySortMapping sMapping = database.getRedFlagUserHistorySortMapping();
		
		User john = database.getUserMapping().fetchOrCreateByName("john");
		Username johnUsername = database.getUsernameMapping().fetchByUsername("john");
		
		RedFlagReport report = new RedFlagReport(-1, johnUsername.id, "asdf", new Timestamp(System.currentTimeMillis()), new Timestamp(now - 100 * step), null);
		database.getRedFlagReportMapping().save(report);
		

		link = new RedFlagUserHistoryLink(-1, report.id, john.id, "t3_link", "title", "url", null, "permalink", "subreddit", new Timestamp(now - 50 * step), null);
		lMapping.save(link);
		
		comment = new RedFlagUserHistoryComment(-1, report.id, john.id, "t1_comment", "http://www.site.com/link", "the body of the comment", "subreddit", new Timestamp(now - 55 * step), null);
		cMapping.save(comment);
		
		comment2 = new RedFlagUserHistoryComment(-1, report.id, john.id, "t1_comment2", "permalink2", "body2", "subreddit", new Timestamp(now - 45 * step), null);
		cMapping.save(comment2);
		
		link2 = new RedFlagUserHistoryLink(-1, report.id, john.id, "t3_link2", "title2", "Url2", null, "permalink2", "subreddit", new Timestamp(now - 40 * step), null);
		lMapping.save(link2);
		
		MysqlTestUtils.assertListContents(sMapping.fetchAll());
		
		sMapping.produceSort(database, report.id);
		
		assertEquals(4, sMapping.fetchAll().size());
		
		int sort = -1;
		RedFlagUserHistorySort fromDb = sMapping.fetchNext(report.id, sort);
		assertTrue(fromDb.sort > sort);
		assertEquals(report.id, fromDb.reportId);
		assertEquals(RedFlagUserHistorySort.RedFlagUserThing.Comment, fromDb.table);
		assertEquals(comment.id, fromDb.foreignId);
		sort = fromDb.sort;
		
		fromDb = sMapping.fetchNext(report.id, sort);
		assertTrue(fromDb.sort > sort);
		assertEquals(report.id, fromDb.reportId);
		assertEquals(RedFlagUserHistorySort.RedFlagUserThing.Link, fromDb.table);
		assertEquals(link.id, fromDb.foreignId);
		sort = fromDb.sort;
		
		fromDb = sMapping.fetchNext(report.id, sort);
		assertTrue(fromDb.sort > sort);
		assertEquals(report.id, fromDb.reportId);
		assertEquals(RedFlagUserHistorySort.RedFlagUserThing.Comment, fromDb.table);
		assertEquals(comment2.id, fromDb.foreignId);
		sort = fromDb.sort;

		fromDb = sMapping.fetchNext(report.id, sort);
		assertTrue(fromDb.sort > sort);
		assertEquals(report.id, fromDb.reportId);
		assertEquals(RedFlagUserHistorySort.RedFlagUserThing.Link, fromDb.table);
		assertEquals(link2.id, fromDb.foreignId);
		
		fromDb.sort++;
		sMapping.save(fromDb);
		
		RedFlagUserHistorySort fromDb2 = sMapping.fetchNext(report.id, sort);
		assertEquals(fromDb, fromDb2);
	}
}
