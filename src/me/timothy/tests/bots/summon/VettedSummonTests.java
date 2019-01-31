package me.timothy.tests.bots.summon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.functions.IInviteToLendersCampFunction;
import me.timothy.bots.models.PromotionBlacklist;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.bots.summon.VettedSummon;
import me.timothy.jreddit.info.Message;

public class VettedSummonTests {
	private class MockInviteToLendersCamp implements IInviteToLendersCampFunction {
		public Queue<String> expected;
		
		public MockInviteToLendersCamp() {
			expected = new ArrayDeque<>();
		}
		
		public void verify() {
			if(!expected.isEmpty()) {
				StringBuilder missing = new StringBuilder();
				for(int i = 0; !expected.isEmpty(); i++) {
					if(i != 0) {
						missing.append(", ");
					}
					
					missing.append(expected.poll());
				}
				
				fail("Failed to get lenders camp invitations for the following: " + missing);
			}
		}
		
		@Override
		public void inviteToLendersCamp(Username username) {
			assertFalse(expected.isEmpty());
			assertEquals(expected.poll(), username.username);
		}
	}
	
	private VettedSummon summon;
	private MockInviteToLendersCamp mockInviter;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	private Timestamp now;

	@Before 
	public void setUp() throws Exception {
		mockInviter = new MockInviteToLendersCamp();
		summon = new VettedSummon(mockInviter);
		database = SummonTestUtils.getTestDatabase();
		config = SummonTestUtils.getTestConfig();
		now = new Timestamp(System.currentTimeMillis());
	}

	@After
	public void tearDown() {
		database.disconnect();
		database = null;
	}
	
	@Test
	public void testTest() {
		assertNotNull(summon);
	}

	@Test
	public void testDoesntRespondToMiscPMFromRando() {
		Message message = SummonTestUtils.createPM("Hey my guy", "$vetting is cool", "johndoe");
		assertNull(summon.handlePM(message, database, config));
	}
	
	@Test
	public void testDoesntRespondToMiscPMFromMod() {
		User foxK56 = database.getUserMapping().fetchOrCreateByName("FoxK56");
		foxK56.auth = 5;
		database.getUserMapping().save(foxK56);
		
		Message message = SummonTestUtils.createPM("You cool?", "You dont seem very talkative", "FoxK56");
		assertNull(summon.handlePM(message, database, config));
	}
	
	@Test
	public void testDoesntRespondToCommentOnThreadWithoutToken() {
		User foxK56 = database.getUserMapping().fetchOrCreateByName("FoxK56");
		foxK56.auth = 5;
		database.getUserMapping().save(foxK56);

		Message message = SummonTestUtils.createPM("re: Vetting Required: /u/johndoe", "Seems alright", "FoxK56");
		assertNull(summon.handlePM(message, database, config));
	}
	
	@Test
	public void testHandlesNormalVettingFromMod() {
		database.getResponseMapping().save(new Response(-1, "vetted_user_vetted_success_body", "valid & vetted <user> by request from <author>", now, now));
		
		User bot = database.getUserMapping().fetchOrCreateByName("LoansBot");
		User badguy = database.getUserMapping().fetchOrCreateByName("badguy");
		User mod = database.getUserMapping().fetchOrCreateByName("FoxK56");
		mod.auth = 5;
		database.getUserMapping().save(mod);
		
		database.getPromotionBlacklistMapping().save(new PromotionBlacklist(-1, badguy.id, bot.id, "Vetting required", 
				new Timestamp(System.currentTimeMillis()), null));
		
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		Message message = SummonTestUtils.createPM("re: Vetting Required: /u/badguy", "$vetted success", "FoxK56");
		
		mockInviter.expected.add("badguy");
		SummonResponse resp = summon.handlePM(message, database, config);
		mockInviter.verify();
		
		assertNotNull(resp);
		assertNotNull(resp.getResponseMessage());
		assertFalse(database.getPromotionBlacklistMapping().contains(badguy.id));
	}

	
	@Test
	public void testHandlesNormalVettingFromSub() {
		database.getResponseMapping().save(new Response(-1, "vetted_user_vetted_success_body", "valid & vetted <user> by request from <author>", now, now));
		
		User bot = database.getUserMapping().fetchOrCreateByName("LoansBot");
		User badguy = database.getUserMapping().fetchOrCreateByName("badguy");
		
		database.getPromotionBlacklistMapping().save(new PromotionBlacklist(-1, badguy.id, bot.id, "Vetting required", 
				new Timestamp(System.currentTimeMillis()), null));
		
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		Message message = SummonTestUtils.createPMFromSub("re: Vetting Required: /u/badguy", "$vetted success", "borrow");
		
		mockInviter.expected.add("badguy");
		SummonResponse resp = summon.handlePM(message, database, config);
		mockInviter.verify();
		
		assertNotNull(resp);
		assertNotNull(resp.getResponseMessage());
		assertFalse(database.getPromotionBlacklistMapping().contains(badguy.id));
	}
	
	@Test
	public void testHandlesVettingFailedOneWord() {
		database.getResponseMapping().save(new Response(-1, "vetted_user_not_vetted_success_body", "valid & not vetted <user> by request from <author>; reason: <reason>", now, now));
		
		User bot = database.getUserMapping().fetchOrCreateByName("LoansBot");
		User badguy = database.getUserMapping().fetchOrCreateByName("badguy");
		
		database.getPromotionBlacklistMapping().save(new PromotionBlacklist(-1, badguy.id, bot.id, "Vetting required", 
				new Timestamp(System.currentTimeMillis()), null));
		
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		Message message = SummonTestUtils.createPMFromSub("re: Vetting Required: /u/badguy", "$vetted failure generic", "borrow");
		
		SummonResponse resp = summon.handlePM(message, database, config);
		
		assertNotNull(resp);
		assertNotNull(resp.getResponseMessage());
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		PromotionBlacklist pb = database.getPromotionBlacklistMapping().fetchById(badguy.id);
		assertEquals("generic", pb.reason);
		assertEquals(bot.id, pb.modUserId);
	}

	@Test
	public void testHandlesVettingFailedSingleQuotes() {
		database.getResponseMapping().save(new Response(-1, "vetted_user_not_vetted_success_body", "valid & not vetted <user> by request from <author>; reason: <reason>", now, now));
		
		User bot = database.getUserMapping().fetchOrCreateByName("LoansBot");
		User badguy = database.getUserMapping().fetchOrCreateByName("badguy");
		
		database.getPromotionBlacklistMapping().save(new PromotionBlacklist(-1, badguy.id, bot.id, "Vetting required", 
				new Timestamp(System.currentTimeMillis()), null));
		
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		Message message = SummonTestUtils.createPMFromSub("re: Vetting Required: /u/badguy", "$vetted failure 'this guy has no history except here'", "borrow");
		
		SummonResponse resp = summon.handlePM(message, database, config);
		
		assertNotNull(resp);
		assertNotNull(resp.getResponseMessage());
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		PromotionBlacklist pb = database.getPromotionBlacklistMapping().fetchById(badguy.id);
		assertEquals("this guy has no history except here", pb.reason);
		assertEquals(bot.id, pb.modUserId);
	}

	@Test
	public void testHandlesVettingFailedDoubleQuotes() {
		database.getResponseMapping().save(new Response(-1, "vetted_user_not_vetted_success_body", "valid & not vetted <user> by request from <author>; reason: <reason>", now, now));
		
		User bot = database.getUserMapping().fetchOrCreateByName("LoansBot");
		User badguy = database.getUserMapping().fetchOrCreateByName("badguy");
		
		database.getPromotionBlacklistMapping().save(new PromotionBlacklist(-1, badguy.id, bot.id, "Vetting required", 
				new Timestamp(System.currentTimeMillis()), null));
		
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		Message message = SummonTestUtils.createPMFromSub("re: Vetting Required: /u/badguy", "$vetted failure \"this guy has no 'history' except here\"", "borrow");
		
		SummonResponse resp = summon.handlePM(message, database, config);
		
		assertNotNull(resp);
		assertNotNull(resp.getResponseMessage());
		assertTrue(database.getPromotionBlacklistMapping().contains(badguy.id));
		
		PromotionBlacklist pb = database.getPromotionBlacklistMapping().fetchById(badguy.id);
		assertEquals("this guy has no 'history' except here", pb.reason);
		assertEquals(bot.id, pb.modUserId);
	}
}
