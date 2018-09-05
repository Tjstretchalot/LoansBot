package me.timothy.tests.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.RedFlagQueueSpot;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.Username;

import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

/**
 * A suite of tests focused on testing a RedFlagQueueSpotMapping.
 * The database should be cleared prior to each test. The database 
 * will be modified after each test. Do not run against a production 
 * database.
 * 
 * @author Timothy
 */
public class RedFlagQueueSpotMappingTest {
protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	// you could break this function down more but it's a particularly simple class
	@Test
	public void testAll() {
		final long now = System.currentTimeMillis();
		
		database.getUserMapping().fetchOrCreateByName("paul");
		database.getUserMapping().fetchOrCreateByName("john");
		database.getUserMapping().fetchOrCreateByName("alex");
		
		Username paulUsername = database.getUsernameMapping().fetchByUsername("paul");
		Username johnUsername = database.getUsernameMapping().fetchByUsername("john");
		Username alexUsername = database.getUsernameMapping().fetchByUsername("alex");
		
		RedFlagReport paulReport = new RedFlagReport(-1, paulUsername.id, null, new Timestamp(now), null, null);
		RedFlagReport johnReport = new RedFlagReport(-1, johnUsername.id, null, new Timestamp(now), null, null);
		
		database.getRedFlagReportMapping().save(paulReport);
		database.getRedFlagReportMapping().save(johnReport);
		
		assertNull(database.getRedFlagQueueSpotMapping().fetchOldestUncompleted());
		assertListContents(database.getRedFlagQueueSpotMapping().fetchAll());
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(paulReport.id, true));
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(johnReport.id, true));
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(paulReport.id, false));
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(johnReport.id, false));
		
		RedFlagQueueSpot paulSpot = new RedFlagQueueSpot(-1, paulReport.id, paulUsername.id, new Timestamp(now), null, null);
		database.getRedFlagQueueSpotMapping().save(paulSpot);
		
		assertNotEquals(-1, paulSpot.id);
		
		assertListContents(database.getRedFlagQueueSpotMapping().fetchAll(), paulSpot);
		
		RedFlagQueueSpot fromDb = database.getRedFlagQueueSpotMapping().fetchOldestUncompleted();
		assertEquals(paulSpot, fromDb);
		
		fromDb = database.getRedFlagQueueSpotMapping().fetchByID(paulSpot.id);
		assertEquals(paulSpot, fromDb);
		
		RedFlagQueueSpot johnSpot = new RedFlagQueueSpot(-1, null, johnUsername.id, new Timestamp(now + 5000), null, null);
		database.getRedFlagQueueSpotMapping().save(johnSpot);
		
		assertNotEquals(-1, johnSpot.id);
		assertNotEquals(paulSpot.id, johnSpot.id);
		
		assertListContents(database.getRedFlagQueueSpotMapping().fetchAll(), paulSpot, johnSpot);
		
		fromDb = database.getRedFlagQueueSpotMapping().fetchOldestUncompleted();
		assertEquals(fromDb, paulSpot);
		
		paulSpot.startedAt = new Timestamp(now + 10000);
		paulSpot.completedAt = new Timestamp(now + 15000);
		database.getRedFlagQueueSpotMapping().save(paulSpot);
		
		fromDb = database.getRedFlagQueueSpotMapping().fetchOldestUncompleted();
		assertEquals(fromDb, johnSpot);
		
		assertListContents(database.getRedFlagQueueSpotMapping().fetchAll(), paulSpot, johnSpot);
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(paulReport.id, true));
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(paulReport.id, false), paulSpot);
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(johnReport.id, true));
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(johnReport.id, false));
		
		johnSpot.reportId = johnReport.id;
		database.getRedFlagQueueSpotMapping().save(johnSpot);

		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(johnReport.id, true), johnSpot);
		assertListContents(database.getRedFlagQueueSpotMapping().fetchByReportId(johnReport.id, false), johnSpot);
		
		RedFlagQueueSpot alexSpot = new RedFlagQueueSpot(-1, null, alexUsername.id, new Timestamp(now + 30000), null, null);
		database.getRedFlagQueueSpotMapping().save(alexSpot);
		
		assertNotEquals(-1, alexSpot.id);
		assertNotEquals(paulSpot.id, alexSpot.id);
		assertNotEquals(johnSpot.id, alexSpot.id);
	}
}
