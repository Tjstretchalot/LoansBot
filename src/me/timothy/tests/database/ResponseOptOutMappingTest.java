package me.timothy.tests.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.ResponseOptOut;
import me.timothy.bots.models.User;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class ResponseOptOutMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		ResponseOptOut paulOptOut = new ResponseOptOut(-1, paul.id, new Timestamp(System.currentTimeMillis()));
		database.getResponseOptOutMapping().save(paulOptOut);
		assertTrue(paulOptOut.id > 0);
		
		MysqlTestUtils.assertListContents(database.getResponseOptOutMapping().fetchAll(), paulOptOut);
	}
	
	@Test
	public void testContains() {
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		assertFalse(database.getResponseOptOutMapping().contains(john.id));
		assertFalse(database.getResponseOptOutMapping().contains(paul.id));
		
		database.getResponseOptOutMapping().save(new ResponseOptOut(-1, paul.id, new Timestamp(System.currentTimeMillis())));
		
		assertFalse(database.getResponseOptOutMapping().contains(john.id));
		assertTrue(database.getResponseOptOutMapping().contains(paul.id));
	}
}
