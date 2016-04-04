package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.Fullname;

/**
 * Describes tests focused on the FullnameMapping in 
 * a mapping database. The database <i>must</i> be completely
 * empty after setup every time. The database <i>will</i> be modified
 * - it should point at a test database.
 * 
 * @author Timothy
 */
public class FullnameMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		final String fullnameStr = "asdfgh";
		Fullname fullname = new Fullname(-1, fullnameStr);
		
		database.getFullnameMapping().save(fullname);
		
		assertTrue(fullname.id > 0);
		assertTrue(fullname.fullname.equals(fullnameStr));
		
		List<Fullname> fromDb = database.getFullnameMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue(fromDb.toString() + " found; expected " + fullname, fromDb.contains(fullname));
	}
	
	@Test
	public void testContains() {
		final String fullnameStr = "asdfgh";
		
		assertFalse(database.getFullnameMapping().contains(fullnameStr));
		
		database.getFullnameMapping().save(new Fullname(-1, fullnameStr));
		
		assertTrue(database.getFullnameMapping().contains(fullnameStr));
		assertFalse(database.getFullnameMapping().contains("asdf%"));
	}
}
