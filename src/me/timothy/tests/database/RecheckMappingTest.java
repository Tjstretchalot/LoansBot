package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.Recheck;

/**
 * Describes a test that focuses on testing a RecheckMapping in
 * a MappingDatabase. The database should be cleared prior to each
 * test. The database will be modified after each test. Do not run
 * against a production database.
 * 
 * @author Timothy
 */
public class RecheckMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		Recheck recheck = new Recheck();
		recheck.id = -1;
		recheck.fullname = "t1_asdf";
		recheck.createdAt = new Timestamp(System.currentTimeMillis());
		recheck.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getRecheckMapping().save(recheck);
		assertTrue(recheck.id > 0);
		
		List<Recheck> fromDb = database.getRecheckMapping().fetchAll();
		assertListContents(fromDb, recheck);
	}
	
	@Test
	public void testDelete() {
		Recheck r1 = new Recheck();
		r1.id = -1;
		r1.fullname = "t1_asdf";
		r1.createdAt = new Timestamp(System.currentTimeMillis());
		r1.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getRecheckMapping().save(r1);
		
		List<Recheck> fromDb = database.getRecheckMapping().fetchAll();
		assertListContents(fromDb, r1);
		
		database.getRecheckMapping().delete(r1);
		
		fromDb = database.getRecheckMapping().fetchAll();
		assertEquals(0, fromDb.size());
		
		r1.id = -1;
		database.getRecheckMapping().save(r1);
		
		// Note the matching fullnames - that shouldn't cause problems
		Recheck r2 = new Recheck();
		r2.id = -1;
		r2.fullname = "t1_asdf";
		r2.createdAt = new Timestamp(System.currentTimeMillis());
		r2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getRecheckMapping().save(r2);
		
		fromDb = database.getRecheckMapping().fetchAll();
		assertListContents(fromDb, r1, r2);
		
		database.getRecheckMapping().delete(r2);
		
		fromDb = database.getRecheckMapping().fetchAll();
		assertListContents(fromDb, r1);
		
		database.getRecheckMapping().delete(r1);
		
		fromDb = database.getRecheckMapping().fetchAll();
		assertEquals(0, fromDb.size());
	}
	
	
}
