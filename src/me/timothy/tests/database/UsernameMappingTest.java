package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;

/**
 * Describes tests for a UsernameMapping in a MappingDatabase. The database
 * must be <i>completely</i> empty after each setUp. The database <i>will</i> 
 * be modified. Do not run against an actual database.
 * 
 * @author Timothy
 */
public class UsernameMappingTest {
	protected MappingDatabase database;
	
	/**
	 * Generates &amp; saves a new user
	 * @return the user
	 */
	protected User createUser() {
		User user = new User();
		user.id = -1;
		user.createdAt = new Timestamp(System.currentTimeMillis());
		user.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getUserMapping().save(user);
		return user;
	}
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User paulUser = createUser();
		Username paul = new Username(-1, paulUser.id, "paul", 
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(paul);
		assertTrue(paul.id > 0);
		
		List<Username> fromDb = database.getUsernameMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + paul, fromDb.contains(paul));
	}
	
	@Test
	public void testFetchById() {
		User paulUser = createUser();
		Username paul = new Username(-1, paulUser.id, "paul", 
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(paul);
		
		Username fromDb = database.getUsernameMapping().fetchById(paul.id);
		assertEquals(paul, fromDb);
		
		Username paulNickname = new Username(-1, paulUser.id, "P.J.", 
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(paulNickname);
		
		fromDb = database.getUsernameMapping().fetchById(paul.id);
		assertEquals(paul, fromDb);
		
		fromDb = database.getUsernameMapping().fetchById(paulNickname.id);
		assertEquals(paulNickname, fromDb);
	}
	
	@Test
	public void testFetchByUserId() {
		User paulUser = createUser();
		Username paul = new Username(-1, paulUser.id, "paul", 
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(paul);
		
		List<Username> fromDb = database.getUsernameMapping().fetchByUserId(paulUser.id);
		assertEquals(1, fromDb.size());
		assertTrue(fromDb.contains(paul));
		
		Username paulNickname = new Username(-1, paulUser.id, "P.J.", 
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(paulNickname);
		
		fromDb = database.getUsernameMapping().fetchByUserId(paulUser.id);
		assertEquals(2, fromDb.size());
		assertTrue(fromDb.contains(paul));
		assertTrue(fromDb.contains(paulNickname));
		
		User gregUser = createUser();
		Username greg = new Username(-1, gregUser.id, "greg", 
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(greg);
		
		fromDb = database.getUsernameMapping().fetchByUserId(gregUser.id);
		assertEquals(1, fromDb.size());
		assertTrue(fromDb.contains(greg));
	}
	
	@Test
	public void testFetchByUsername() {
		User paulUser = createUser();
		Username paul = new Username(-1, paulUser.id, "paul",
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(paul);
		
		Username fromDb = database.getUsernameMapping().fetchByUsername(paul.username);
		assertEquals(paul, fromDb);
		
		fromDb = database.getUsernameMapping().fetchByUsername("not_a_username");
		assertNull(fromDb);
		
		// Don't use likeness tests
		fromDb = database.getUsernameMapping().fetchByUsername("pa%"); 
		assertNull(fromDb);
		
		Username paulNickname = new Username(-1, paulUser.id, "P.J.",
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getUsernameMapping().save(paulNickname);
		
		fromDb = database.getUsernameMapping().fetchByUsername(paul.username);
		assertEquals(paul, fromDb);
		
		fromDb = database.getUsernameMapping().fetchByUsername(paulNickname.username);
		assertEquals(paulNickname, fromDb);
	}
}

