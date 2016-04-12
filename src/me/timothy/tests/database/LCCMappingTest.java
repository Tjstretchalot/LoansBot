package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.LendersCampContributor;
import me.timothy.bots.models.User;

/**
 * Describes a test focused on testing a lenders camp contributor
 * mapper in a mapping database. The database must be cleared prior
 * to each test. The database will be modified. Do not run on a 
 * production database.
 * 
 * @author Timothy
 */
public class LCCMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		LendersCampContributor lccPaul = new LendersCampContributor();
		lccPaul.id = -1;
		lccPaul.userId = paul.id;
		lccPaul.botAdded = true;
		lccPaul.createdAt = new Timestamp(System.currentTimeMillis());
		lccPaul.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLccMapping().save(lccPaul);
		assertTrue(lccPaul.id > 0);
		
		List<LendersCampContributor> fromDb = database.getLccMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + lccPaul, fromDb.contains(lccPaul));
		
		LendersCampContributor lccJohn = new LendersCampContributor();
		lccJohn.id = -1;
		lccJohn.userId = john.id;
		lccJohn.botAdded = false;
		lccJohn.createdAt = new Timestamp(System.currentTimeMillis());
		lccJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLccMapping().save(lccJohn);
		assertTrue(lccJohn.id > 0);
		
		fromDb = database.getLccMapping().fetchAll();
		assertEquals(2, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + lccPaul, fromDb.contains(lccPaul));
		assertTrue("expected " + fromDb + " to contain " + lccJohn, fromDb.contains(lccJohn));
	}
	
	@Test
	public void testContains() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		assertFalse(database.getLccMapping().contains(paul.id));
		
		LendersCampContributor lccPaul = new LendersCampContributor();
		lccPaul.id = -1;
		lccPaul.userId = paul.id;
		lccPaul.botAdded = true;
		lccPaul.createdAt = new Timestamp(System.currentTimeMillis());
		lccPaul.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLccMapping().save(lccPaul);
		
		LendersCampContributor lccJohn = new LendersCampContributor();
		lccJohn.id = -1;
		lccJohn.userId = john.id;
		lccJohn.botAdded = false;
		lccJohn.createdAt = new Timestamp(System.currentTimeMillis());
		lccJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLccMapping().save(lccJohn);
		
		assertTrue(database.getLccMapping().contains(paul.id));
		assertTrue(database.getLccMapping().contains(john.id));
		assertFalse(database.getLccMapping().contains(greg.id));
	}
}
