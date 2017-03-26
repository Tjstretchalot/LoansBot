package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.BannedUser;
import me.timothy.bots.models.User;

/**
 * Describes tests focused on testing an BannedUserMapping inside
 * a MappingDatabase. The database should be <i>completely</i> empty
 * before each test. The database <i>will</i> be modified. This should
 * <i>not</i> be run against a production database.
 * 
 * @author Timothy
 */
public class BannedUserMappingTest {
	/**
	 * The mapping database containing the 
	 * {@link me.timothy.bots.database.AdminUpdateMapping AdminUpdateMapping} 
	 * to be tested. Must be initialized by a child class.
	 */
	protected MappingDatabase database;
	
	/**
	 * <p>Verifies that the test has been prepared correctly
	 * by the subclass by asserting that the {@link #database database} is 
	 * not null.</p>
	 */
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	/**
	 * <p>Tests the ability to save {@link me.timothy.bots.models.BannedUser BannedUsers}</p>
	 * 
	 * <p>This is a basic test to ensure that doing a standard {@link me.timothy.bots.database.ObjectMapping#save(Object) save}
	 * does not result in any errors, and sets the {@link me.timothy.bots.models.BannedUser#id id} of the user to the
	 * database, and the very same {@link me.timothy.bots.models.BannedUser BannedUser} is returned, compared with
	 * {@link me.timothy.bots.models.BannedUser#equals(Object) equals}, fetched with {@link me.timothy.bots.database.ObjectMapping#fetchAll() fetchAll}
	 * </p>
	 * 
	 * <p>This has the side-effect of checking 
	 * {@link me.timothy.bots.models.BannedUser#equals(Object) BannedUsers equals function} is implemented.</p> 
	 */
	@Test
	public void testSave() {
		User user = database.getUserMapping().fetchOrCreateByName("john");
		database.getUserMapping().save(user);
		
		BannedUser banned = new BannedUser(-1, user.id, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getBannedUserMapping().save(banned);
		
		assertTrue(banned.id > 0);
		
		List<BannedUser> fromDB = database.getBannedUserMapping().fetchAll();
		assertEquals(1, fromDB.size());
		
		assertEquals(banned, fromDB.get(0));
	}
	
	/**
	 * <p>This test ensures that {@link me.timothy.bots.database.BannedUserMapping#containsUserID(int) containsUserID}
	 * returns false before the user is banned, and true afterwards</p>
	 */
	@Test
	public void testContainsUserID() {
		User user = database.getUserMapping().fetchOrCreateByName("john");
		database.getUserMapping().save(user);

		assertFalse(database.getBannedUserMapping().containsUserID(user.id));

		BannedUser banned = new BannedUser(-1, user.id, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getBannedUserMapping().save(banned);

		assertTrue(database.getBannedUserMapping().containsUserID(user.id));
	}
	
	/**
	 * <p>This tests {@link me.timothy.bots.database.BannedUserMapping#removeByUserID(int) removeByUserID} to ensure it removes
	 * the user, and only the user, with the specified id</p>
	 */
	@Test
	public void testRemoveByUserID() {
		User user = database.getUserMapping().fetchOrCreateByName("john");
		database.getUserMapping().save(user);
		
		User user2 = database.getUserMapping().fetchOrCreateByName("greg");
		database.getUserMapping().save(user2);
		
		assertFalse(database.getBannedUserMapping().containsUserID(user.id));

		BannedUser banned = new BannedUser(-1, user.id, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getBannedUserMapping().save(banned);

		assertTrue(database.getBannedUserMapping().containsUserID(user.id));
		assertFalse(database.getBannedUserMapping().containsUserID(user2.id));
		
		BannedUser banned2 = new BannedUser(-1, user2.id, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		database.getBannedUserMapping().save(banned2);
		
		assertTrue(database.getBannedUserMapping().containsUserID(user.id));
		assertTrue(database.getBannedUserMapping().containsUserID(user2.id));
		
		database.getBannedUserMapping().removeByUserID(user.id);

		assertFalse(database.getBannedUserMapping().containsUserID(user.id));
		assertTrue(database.getBannedUserMapping().containsUserID(user2.id));
		
	}
}
