package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;

import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

/**
 * Describes a suite of tests focusing on a RedFlagReportMapping
 * The database should be cleared prior to each test. The database 
 * will be modified after each test. Do not run against a production 
 * database.
 * 
 * @author Timothy
 */
public class RedFlagReportMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	// you could break this function down more but it's a particularly simple class
	@Test
	public void testAll() {
		final long now = System.currentTimeMillis();
		
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		Username paulUsername = database.getUsernameMapping().fetchByUserId(paul.id).get(0);
		
		RedFlagReport paulReport = new RedFlagReport(-1, paulUsername.id, null, new Timestamp(now), null, null);
		assertTrue(paulReport.isValid());
		
		database.getRedFlagReportMapping().save(paulReport);
		
		assertNotEquals(paulReport.id, -1);
		assertEquals(paulReport.createdAt.getNanos(), 0);
		
		RedFlagReport fromDb = database.getRedFlagReportMapping().fetchByID(paulReport.id);
		assertEquals(paulReport, fromDb);
		
		int oldId = paulReport.id;
		paulReport.startedAt = new Timestamp(now + 5000);
		database.getRedFlagReportMapping().save(paulReport);
		
		assertEquals(oldId, paulReport.id);
		
		fromDb = database.getRedFlagReportMapping().fetchByID(paulReport.id);
		assertEquals(paulReport, fromDb);
		
		User john = database.getUserMapping().fetchOrCreateByName("john");
		Username johnUsername = database.getUsernameMapping().fetchByUserId(john.id).get(0);
		
		RedFlagReport johnReport = new RedFlagReport(-1, johnUsername.id, null, new Timestamp(now + 10000), null, null);
		database.getRedFlagReportMapping().save(johnReport);
		
		assertNotEquals(johnReport.id, -1);
		assertNotEquals(johnReport.id, paulReport.id);
		
		fromDb = database.getRedFlagReportMapping().fetchByID(paulReport.id);
		assertEquals(paulReport, fromDb);
		
		fromDb = database.getRedFlagReportMapping().fetchByID(johnReport.id);
		assertEquals(johnReport, fromDb);
		
		List<RedFlagReport> listFromDb = database.getRedFlagReportMapping().fetchAll();
		assertListContents(listFromDb, paulReport, johnReport);
		
		
		listFromDb = database.getRedFlagReportMapping().fetchByUsernameID(paulUsername.id);
		assertListContents(listFromDb, paulReport);
		
		listFromDb = database.getRedFlagReportMapping().fetchByUsernameID(johnUsername.id);
		assertListContents(listFromDb, johnReport);
	}
	
}
