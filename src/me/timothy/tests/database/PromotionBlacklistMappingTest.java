package me.timothy.tests.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.PromotionBlacklist;
import me.timothy.bots.models.User;
import me.timothy.tests.database.mysql.MysqlTestUtils;

public class PromotionBlacklistMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testAll() {
		long now = System.currentTimeMillis();
		
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		User mod = database.getUserMapping().fetchOrCreateByName("moder");
		
		List<PromotionBlacklist> fromDb = database.getPromotionBlacklistMapping().fetchAll();
		MysqlTestUtils.assertListContents(fromDb);
		
		PromotionBlacklist gregList = new PromotionBlacklist(-1, greg.id, mod.id, "likes onions", new Timestamp(now), null);
		database.getPromotionBlacklistMapping().save(gregList);
		
		assertNotEquals(-1, gregList.id);
		assertEquals(0, gregList.addedAt.getNanos());
		
		fromDb = database.getPromotionBlacklistMapping().fetchAll();
		MysqlTestUtils.assertListContents(fromDb, gregList);
		
		assertTrue(database.getPromotionBlacklistMapping().contains(greg.id));
		assertFalse(database.getPromotionBlacklistMapping().contains(john.id));
		
		gregList.reason = "hates onions";
		database.getPromotionBlacklistMapping().save(gregList);
		
		fromDb = database.getPromotionBlacklistMapping().fetchAll();
		MysqlTestUtils.assertListContents(fromDb, gregList);
		assertEquals(fromDb.get(0).reason, "hates onions");
		
		gregList.removedAt = new Timestamp(now + 5000);
		database.getPromotionBlacklistMapping().save(gregList);
		
		assertFalse(database.getPromotionBlacklistMapping().contains(greg.id));
		assertFalse(database.getPromotionBlacklistMapping().contains(john.id));
		
		PromotionBlacklist gregList2 = new PromotionBlacklist(-1, greg.id, mod.id, "still likes onions", new Timestamp(now + 10000), null);
		database.getPromotionBlacklistMapping().save(gregList2);
		
		assertTrue(database.getPromotionBlacklistMapping().contains(greg.id));
		assertFalse(database.getPromotionBlacklistMapping().contains(john.id));
		assertFalse(database.getPromotionBlacklistMapping().contains(mod.id));
		
		fromDb = database.getPromotionBlacklistMapping().fetchAll();
		MysqlTestUtils.assertListContents(fromDb, gregList, gregList2);
		
		gregList2.removedAt = new Timestamp(now + 12000);
		database.getPromotionBlacklistMapping().save(gregList2);
		
		fromDb = database.getPromotionBlacklistMapping().fetchAll();
		MysqlTestUtils.assertListContents(fromDb, gregList, gregList2);
		
		assertFalse(database.getPromotionBlacklistMapping().contains(greg.id));
		assertFalse(database.getPromotionBlacklistMapping().contains(john.id));
		assertFalse(database.getPromotionBlacklistMapping().contains(mod.id));
	}
}
