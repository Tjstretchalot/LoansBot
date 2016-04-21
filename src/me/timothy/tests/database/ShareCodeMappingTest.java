package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.ShareCode;
import me.timothy.bots.models.User;

/**
 * Describes a test focused on testing a ShareCodeMapping inside a
 * MappingDatabase. The database must be cleared prior to each test. 
 * The database will be modified in each test. Do not run against a
 * production database.
 * 
 * @author Timothy
 */
public class ShareCodeMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		ShareCode shareCode = new ShareCode();
		shareCode.id = -1;
		shareCode.userId = paul.id;
		shareCode.code = "asdf";
		shareCode.updatedAt = new Timestamp(System.currentTimeMillis());
		shareCode.createdAt = new Timestamp(System.currentTimeMillis());
		database.getShareCodeMapping().save(shareCode);
		assertTrue(shareCode.id > 0);
		
		List<ShareCode> fromDb = database.getShareCodeMapping().fetchAll();
		assertListContents(fromDb, shareCode);
	}
	
	@Test 
	public void testFetchForUser() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		ShareCode gregSC = new ShareCode();
		gregSC.id = -1;
		gregSC.userId = greg.id;
		gregSC.code = "Look ma, no hands!";
		gregSC.updatedAt = new Timestamp(System.currentTimeMillis());
		gregSC.createdAt = new Timestamp(System.currentTimeMillis());
		database.getShareCodeMapping().save(gregSC);
		
		ShareCode paulSC = new ShareCode();
		paulSC.id = -1;
		paulSC.userId = paul.id;
		paulSC.code = "klasjd";
		paulSC.updatedAt = new Timestamp(System.currentTimeMillis());
		paulSC.createdAt = new Timestamp(System.currentTimeMillis());
		database.getShareCodeMapping().save(paulSC);
		
		List<ShareCode> fromDb = database.getShareCodeMapping().fetchForUser(-1);
		assertEquals(0, fromDb.size());
		
		fromDb = database.getShareCodeMapping().fetchForUser(paul.id);
		assertListContents(fromDb, paulSC);
		
		fromDb = database.getShareCodeMapping().fetchForUser(greg.id);
		assertListContents(fromDb, gregSC);
	}
	
	@Test
	public void testDelete() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		ShareCode shareCode = new ShareCode();
		shareCode.id = -1;
		shareCode.userId = paul.id;
		shareCode.code = "asdf";
		shareCode.updatedAt = new Timestamp(System.currentTimeMillis());
		shareCode.createdAt = new Timestamp(System.currentTimeMillis());
		database.getShareCodeMapping().save(shareCode);
		assertTrue(shareCode.id > 0);
		
		List<ShareCode> fromDb = database.getShareCodeMapping().fetchAll();
		assertListContents(fromDb, shareCode);
		
		database.getShareCodeMapping().delete(shareCode);
		shareCode = null;
		
		fromDb = database.getShareCodeMapping().fetchAll();
		assertEquals(0, fromDb.size());
	}

}
