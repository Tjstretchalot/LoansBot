package me.timothy.tests.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;

import me.timothy.bots.database.DelayedVettingRequestMapping;
import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.DelayedVettingRequest;
import me.timothy.bots.models.User;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class DelayedVettingRequestMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testAll() {
		User bot = database.getUserMapping().fetchOrCreateByName("LoansBot");
		User us1 = database.getUserMapping().fetchOrCreateByName("user1");
		User us2 = database.getUserMapping().fetchOrCreateByName("user2");
		
		DelayedVettingRequestMapping map = database.getDelayedVettingRequestMapping();
		
		assertNull(map.fetchByUserId(bot.id));
		assertNull(map.fetchByUserId(us1.id));
		assertNull(map.fetchByUserId(us2.id));
		MysqlTestUtils.assertListContents(map.fetchAll());
		
		DelayedVettingRequest req1 = new DelayedVettingRequest(-1, us1.id, 15, "blah", new Timestamp(System.currentTimeMillis()), null);
		assertEquals(-1, req1.id);
		assertEquals(req1, req1);
		assertTrue(req1.isValid());
		map.save(req1);
		assertTrue(req1.id > 0);

		assertNull(map.fetchByUserId(bot.id));
		assertEquals(req1, map.fetchByUserId(us1.id));
		assertNull(map.fetchByUserId(us2.id));
		MysqlTestUtils.assertListContents(map.fetchAll(), req1);
		
		req1.rerequestedAt = new Timestamp(System.currentTimeMillis());
		map.save(req1);

		assertNull(map.fetchByUserId(bot.id));
		assertNull(map.fetchByUserId(us1.id));
		assertNull(map.fetchByUserId(us2.id));
		MysqlTestUtils.assertListContents(map.fetchAll(), req1);
		
		DelayedVettingRequest req2 = new DelayedVettingRequest(-1, us1.id, 25, "berp", new Timestamp(System.currentTimeMillis() + 1000), null);
		map.save(req2);
		assertNotEquals(req1.id, req2.id);

		assertNull(map.fetchByUserId(bot.id));
		assertEquals(req2, map.fetchByUserId(us1.id));
		assertNull(map.fetchByUserId(us2.id));
		MysqlTestUtils.assertListContents(map.fetchAll(), req1, req2);
	}
}
