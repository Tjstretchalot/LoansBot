package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.User;

/**
 * Describes tests focused on a UserMapping in a MappingDatabase. The database
 * must be <i>completely</i> empty after each setUp. The database <i>will</i> be
 * modified after each run. Do not run against a real database.
 * 
 * @author Timothy
 */
public class UserMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User user1 = new User();
		user1.id = -1;
		user1.createdAt = new Timestamp(System.currentTimeMillis());
		user1.updatedAt = new Timestamp(System.currentTimeMillis());
		
		database.getUserMapping().save(user1);
		assertTrue(user1.id > 0);
		
		List<User> fromDb = database.getUserMapping().fetchAll();
		assertListContents(fromDb, user1);
	}
	
	@Test
	public void testFetchById() {
		User user1 = new User();
		user1.id = -1;
		user1.createdAt = new Timestamp(System.currentTimeMillis());
		user1.updatedAt = new Timestamp(System.currentTimeMillis());
		
		database.getUserMapping().save(user1);
		
		User fromDb = database.getUserMapping().fetchById(user1.id);
		assertEquals(user1, fromDb);
		
		User user2 = new User();
		user2.id = -1;
		user2.createdAt = new Timestamp(System.currentTimeMillis());
		user2.updatedAt = new Timestamp(System.currentTimeMillis());
		
		database.getUserMapping().save(user2);
		
		fromDb = database.getUserMapping().fetchById(user1.id);
		assertEquals(user1, fromDb);
		
		fromDb = database.getUserMapping().fetchById(user2.id);
		assertEquals(user2, fromDb);
	}
	
	@Test
	public void testFetchOrCreateByName() {
		User john = database.getUserMapping().fetchOrCreateByName("john");
		assertNotNull(john);
		assertNotNull(john.createdAt);
		assertNotNull(john.updatedAt);
		
		User alsoJohn = database.getUserMapping().fetchOrCreateByName("john");
		assertEquals(john, alsoJohn);
		
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		assertNotNull(greg);
		
		assertNotEquals(john, greg);
		
		alsoJohn = database.getUserMapping().fetchOrCreateByName("john");
		assertEquals(john, alsoJohn);
		
		User alsoGreg = database.getUserMapping().fetchOrCreateByName("greg");
		assertEquals(greg, alsoGreg);
	}
	
	@Test
	public void testMaxUserId() {
		User user1 = new User();
		user1.id = -1;
		user1.createdAt = new Timestamp(System.currentTimeMillis());
		user1.updatedAt = new Timestamp(System.currentTimeMillis());
		
		database.getUserMapping().save(user1);
		
		assertEquals(user1.id, database.getUserMapping().fetchMaxUserId());
		
		User user2 = new User();
		user2.id = -1;
		user2.createdAt = new Timestamp(System.currentTimeMillis());
		user2.updatedAt = new Timestamp(System.currentTimeMillis());
		
		database.getUserMapping().save(user2);
		
		assertEquals(Math.max(user1.id, user2.id), database.getUserMapping().fetchMaxUserId());
	}
	
	@Test
	public void testToSendCodeTo() {
		User user1 = new User();
		user1.id = -1;
		user1.claimed = false;
		user1.claimCode = "asdf";
		user1.claimLinkSentAt = null;
		user1.createdAt = new Timestamp(System.currentTimeMillis());
		user1.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getUserMapping().save(user1);
		
		List<User> fromDb = database.getUserMapping().fetchUsersToSendCode();
		assertListContents(fromDb, user1);
		
		user1.claimLinkSentAt = new Timestamp(System.currentTimeMillis());
		user1.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getUserMapping().save(user1);
		
		fromDb = database.getUserMapping().fetchUsersToSendCode();
		assertEquals(0, fromDb.size());
		
		User user2 = new User();
		user2.id = -1;
		user2.claimed = false;;
		user2.claimCode = "asdf";
		user2.claimLinkSentAt = null;
		user2.createdAt = new Timestamp(System.currentTimeMillis());
		user2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getUserMapping().save(user2);
		
		fromDb = database.getUserMapping().fetchUsersToSendCode();
		assertListContents(fromDb, user2);
	}

}
