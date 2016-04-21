package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

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
	/**
	 * The {@link MappingDatabase} that contains the 
	 * {@link me.timothy.bots.database.LCCMapping LCCMapping}
	 * to test.
	 */
	protected MappingDatabase database;
	
	/**
	 * Ensures the test is set up correctly by verifying that
	 * the {@link #database database} is not null.
	 */
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	/**
	 * Checks that the {@link me.timothy.bots.database.LCCMapping LCCMapping} handles
	 * straightforward {@link me.timothy.bots.database.ObjectMapping#save(Object) saving} by
	 * observing that, when saved, the {@link LendersCampContributor} has a strictly
	 * positive {@link LendersCampContributor#id id}, and when all
	 * {@link LendersCampContributor LendersCampContributors} are fetched using 
	 * {@link me.timothy.bots.database.ObjectMapping#fetchAll() fetchAll}, the only
	 * result is the same {@link LendersCampContributor LCC} that was initially saved,
	 * using an {@link Object#equals(Object) equality} check.
	 */
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
		assertListContents(fromDb, lccPaul);
		
		LendersCampContributor lccJohn = new LendersCampContributor();
		lccJohn.id = -1;
		lccJohn.userId = john.id;
		lccJohn.botAdded = false;
		lccJohn.createdAt = new Timestamp(System.currentTimeMillis());
		lccJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLccMapping().save(lccJohn);
		assertTrue(lccJohn.id > 0);
		
		fromDb = database.getLccMapping().fetchAll();
		assertListContents(fromDb, lccPaul, lccJohn);
	}
	
	/**
	 * Focused on testing that, after a {@link LendersCampContributor LCC}
	 * is saved,
	 * {@link me.timothy.bots.database.LCCMapping#contains(int) LCCMapping#contains(int)}
	 * will return true for the appropriate {@link User user}.{@link User#id id}, and false
	 * for {@link User users} for which no {@link LendersCampContributor LCC} has been created.
	 */
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
