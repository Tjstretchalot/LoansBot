package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Warning;

/**
 * Describes a test focused on testing a WarningMapping in
 * a MappingDatabase. The database must be cleared prior to
 * each test. The database will be modified in each test. Do
 * not run against a production database.
 * 
 * @author Timothy
 */
public class WarningMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Warning warning = new Warning();
		warning.id = -1;
		warning.warningUserId = paul.id;
		warning.warnedUserId = john.id;
		warning.violation = "muh memes";
		warning.actionTaken = "warning";
		warning.nextAction = "temporary ban";
		warning.notes = "used weird symbol for a dissapointed face. thats not a thing right?";
		warning.createdAt = new Timestamp(System.currentTimeMillis());
		warning.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getWarningMapping().save(warning);
		assertTrue(warning.id > 0);
		
		List<Warning> fromDb = database.getWarningMapping().fetchAll();
		assertListContents(fromDb, warning);
	}
	
	@Test
	public void testFetchByWarnedUserId() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Warning warningPaulToGreg = new Warning();
		warningPaulToGreg.id = -1;
		warningPaulToGreg.warningUserId = paul.id;
		warningPaulToGreg.warnedUserId = greg.id;
		warningPaulToGreg.violation = "said mean things for no reason";
		warningPaulToGreg.actionTaken = "warning; deleted comment";
		warningPaulToGreg.nextAction = "ban";
		warningPaulToGreg.notes = "called john a liar. john wouldn't do anything wrong!";
		warningPaulToGreg.createdAt = new Timestamp(System.currentTimeMillis());
		warningPaulToGreg.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getWarningMapping().save(warningPaulToGreg);
		
		List<Warning> fromDb = database.getWarningMapping().fetchByWarnedUserId(paul.id);
		assertEquals(0, fromDb.size());
		
		fromDb = database.getWarningMapping().fetchByWarnedUserId(greg.id);
		assertListContents(fromDb, warningPaulToGreg);
		
		fromDb = database.getWarningMapping().fetchByWarnedUserId(john.id);
		assertEquals(0, fromDb.size());
		
		Warning warningPaulToGreg2 = new Warning();
		warningPaulToGreg2.id = -1;
		warningPaulToGreg2.warningUserId = paul.id;
		warningPaulToGreg2.warnedUserId = greg.id;
		warningPaulToGreg2.violation = "curtliness";
		warningPaulToGreg2.actionTaken = "ban";
		warningPaulToGreg2.nextAction = "-";
		warningPaulToGreg2.notes = "okay no need to use bad words";
		warningPaulToGreg2.createdAt = new Timestamp(System.currentTimeMillis());
		warningPaulToGreg2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getWarningMapping().save(warningPaulToGreg2);
		
		fromDb = database.getWarningMapping().fetchByWarnedUserId(greg.id);
		assertListContents(fromDb, warningPaulToGreg, warningPaulToGreg2);
		
		Warning warningPaulToJohn = new Warning();
		warningPaulToJohn.id = -1;
		warningPaulToJohn.warningUserId = paul.id;
		warningPaulToJohn.warnedUserId = john.id;
		warningPaulToJohn.violation = "pming for loan";
		warningPaulToJohn.actionTaken = "ban";
		warningPaulToJohn.nextAction = "-";
		warningPaulToJohn.notes = "ok maybe greg wasn't wrong after all";
		warningPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		warningPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getWarningMapping().save(warningPaulToJohn);
		
		fromDb = database.getWarningMapping().fetchByWarnedUserId(john.id);
		assertListContents(fromDb, warningPaulToJohn);
	}
}
