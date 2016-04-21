package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.ResponseHistory;
import me.timothy.bots.models.User;

/**
 * Describes a test focused on testing a ResponseHistoryMapping 
 * inside a MappingDatabase. The database must be cleared prior to
 * each test. The database will be modified. Do not run against a
 * production database.
 * 
 * @author Timothy
 */
public class ResponseHistoryMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		Response test = new Response(-1, "hello", "Hello, I'm a test!", new Timestamp(System.currentTimeMillis()), 
				new Timestamp(System.currentTimeMillis()));
		database.getResponseMapping().save(test);
		
		ResponseHistory testHistory = new ResponseHistory();
		testHistory.id = -1;
		testHistory.userId = paul.id;
		testHistory.responseId = test.id;
		testHistory.oldRaw = test.responseBody;
		testHistory.newRaw = "Hello, I'm a test! Are you <name>?";
		testHistory.reason = "verify name";
		testHistory.createdAt = new Timestamp(System.currentTimeMillis());
		testHistory.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getResponseHistoryMapping().save(testHistory);
		assertTrue(testHistory.id > 0);
		
		test.responseBody = testHistory.newRaw;
		database.getResponseMapping().save(test);
		
		List<ResponseHistory> fromDb = database.getResponseHistoryMapping().fetchAll();
		assertListContents(fromDb, testHistory);
	}

	@Test
	public void testFetchForResponse() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		Response test = new Response(-1, "hello", "Hello, I'm a test!", new Timestamp(System.currentTimeMillis()), 
				new Timestamp(System.currentTimeMillis()));
		database.getResponseMapping().save(test);
		
		ResponseHistory testHistory = new ResponseHistory();
		testHistory.id = -1;
		testHistory.userId = paul.id;
		testHistory.responseId = test.id;
		testHistory.oldRaw = test.responseBody;
		testHistory.newRaw = "Hello, I'm a test! Are you <name>?";
		testHistory.reason = "verify name";
		testHistory.createdAt = new Timestamp(System.currentTimeMillis());
		testHistory.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getResponseHistoryMapping().save(testHistory);
		
		test.responseBody = testHistory.newRaw;
		database.getResponseMapping().save(test);
		
		List<ResponseHistory> fromDb = database.getResponseHistoryMapping().fetchForResponse(test.id);
		assertListContents(fromDb, testHistory);
		
		fromDb = database.getResponseHistoryMapping().fetchForResponse(-1);
		assertEquals(0, fromDb.size());
		
		Response goodbye = new Response(-1, "goodbye", "Goodbye!", new Timestamp(System.currentTimeMillis()), 
				new Timestamp(System.currentTimeMillis()));
		database.getResponseMapping().save(goodbye);
		
		ResponseHistory goodbyeHistory = new ResponseHistory();
		goodbyeHistory.id = -1;
		goodbyeHistory.userId = paul.id;
		goodbyeHistory.responseId = goodbye.id;
		goodbyeHistory.oldRaw = goodbye.responseBody;
		goodbyeHistory.newRaw = "Goodbye <name>!";
		goodbyeHistory.reason = "say name";
		goodbyeHistory.createdAt = new Timestamp(System.currentTimeMillis());
		goodbyeHistory.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getResponseHistoryMapping().save(goodbyeHistory);

		fromDb = database.getResponseHistoryMapping().fetchForResponse(test.id);
		assertListContents(fromDb, testHistory);

		fromDb = database.getResponseHistoryMapping().fetchForResponse(goodbye.id);
		assertListContents(fromDb, goodbyeHistory);
	}
}
