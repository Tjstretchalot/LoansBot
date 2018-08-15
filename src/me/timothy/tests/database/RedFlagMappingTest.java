package me.timothy.tests.database;

import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.Username;

/**
 * A suite of tests focused on testing a RedFlagMapping.
 * The database should be cleared prior to each test. The database 
 * will be modified after each test. Do not run against a production 
 * database.
 * 
 * @author Timothy
 */
public class RedFlagMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testAll() {
		final long now = System.currentTimeMillis();
		
		database.getUserMapping().fetchOrCreateByName("paul");
		database.getUserMapping().fetchOrCreateByName("john");
		
		Username paulUsername = database.getUsernameMapping().fetchByUsername("paul");
		Username johnUsername = database.getUsernameMapping().fetchByUsername("john");
		
		RedFlagReport paulReport = new RedFlagReport(-1, paulUsername.id, null, new Timestamp(now), null, null);
		RedFlagReport johnReport = new RedFlagReport(-1, johnUsername.id, null, new Timestamp(now), null, null);
		
		database.getRedFlagReportMapping().save(paulReport);
		database.getRedFlagReportMapping().save(johnReport);
		
		RedFlag paulFlag1 = new RedFlag(-1, paulReport.id, RedFlag.RedFlagType.SUBREDDIT, "iden", "testing", 1, new Timestamp(now));
		database.getRedFlagMapping().save(paulFlag1);
		
		assertNotEquals(-1, paulFlag1.id);
		
		List<RedFlag> fromDb = database.getRedFlagMapping().fetchAll();
		assertListContents(fromDb, paulFlag1);
		
		RedFlag paulFlag2 = new RedFlag(-1, paulReport.id, RedFlag.RedFlagType.SUBREDDIT, "iden", "testing", 1, new Timestamp(now));
		database.getRedFlagMapping().save(paulFlag2);
		
		assertNotEquals(-1, paulFlag2.id);
		assertNotEquals(paulFlag1.id, paulFlag2.id);
		
		fromDb = database.getRedFlagMapping().fetchAll();
		assertListContents(fromDb, paulFlag1, paulFlag2);
		
		fromDb = database.getRedFlagMapping().fetchByReportID(paulReport.id);
		assertListContents(fromDb, paulFlag1, paulFlag2);
		
		fromDb = database.getRedFlagMapping().fetchByReportID(johnReport.id);
		assertListContents(fromDb);
		
		RedFlag johnFlag = new RedFlag(-1, johnReport.id, RedFlag.RedFlagType.NUKED_HISTORY, "iden2", "test---ing", 1, new Timestamp(now + 3000));
		database.getRedFlagMapping().save(johnFlag);
		
		assertNotEquals(-1, johnFlag.id);
		assertNotEquals(paulFlag1.id, johnFlag.id);
		assertNotEquals(paulFlag2.id, johnFlag.id);
		
		fromDb = database.getRedFlagMapping().fetchAll();
		assertListContents(fromDb, paulFlag1, paulFlag2, johnFlag);

		fromDb = database.getRedFlagMapping().fetchByReportID(paulReport.id);
		assertListContents(fromDb, paulFlag1, paulFlag2);
		
		fromDb = database.getRedFlagMapping().fetchByReportID(johnReport.id);
		assertListContents(fromDb, johnFlag);
		
		assertEquals(database.getRedFlagMapping().fetchByReportAndTypeAndIden(johnReport.id, RedFlag.RedFlagType.NUKED_HISTORY.databaseIdentifier, "iden2"), johnFlag);
	}
}
