package me.timothy.tests.bots.summon;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.BotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.User;
import me.timothy.bots.summon.PMResponse;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.bots.summon.UnpaidSummon;
import me.timothy.jreddit.info.Comment;

/**
 * Describes tests focused on a UnpaidSummon.
 * 
 * @author Timothy
 */
public class UnpaidSummonTests {
	private UnpaidSummon summon;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	private Timestamp now;
	
	@Before 
	public void setUp() throws Exception {
		summon = new UnpaidSummon();
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
		assertNotNull(database);
		assertNotNull(config);
		assertNotNull(now);
	}
	
	@Test
	public void testDoesntRespondToMiscComment() {
		Comment comment = SummonTestUtils.createComment("so and so will probably be unpaid and stuff", "johndoe");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	@Test
	public void testHandlesUnpaidWithoutLoan() {
		/*
		 * We will respond with the normal message. The way to indicate that there
		 * was an issue is that there will be nothing under <changed loans>. This
		 * also means that we should notify any affected lenders (though this doesn't
		 * test that)
		 */
		String unpaidFormat = "Sorry about that =(";
		String expectedResponse = unpaidFormat;
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_title", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_text", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_title", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_text", "not testing this", now, now));
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/johndoe", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
	}
	
	@Test
	public void testReplacesStuff() {
		/*
		 * Alex had a loan with johndoe that is outstanding when paul made
		 * an unpaid command with johndoe. No loans are affected, paul is not
		 * banned, alex is alerted, and paul is reminded.
		 */
		final String unpaidFormat = "Sorry about /u/<user1> =(";
		final String reminderPMTitleFormat = "yo <lender> do a thing due to <borrower>";
		final String reminderPMTextFormat = "the incident with /u/<borrower> means you need to do a thing";
		final String affectedUserPMTitleFormat = "oh no a thing with /u/<borrower>";
		final String affectedUserPMTextFormat = "<affected>, a thing occurred with /u/<borrower> - he defaulted on a loan with <lender> at <comment permalink>";
		
		String expectedResponse = "Sorry about /u/johndoe =(";
		String expectedAffectedTitle = "oh no a thing with /u/johndoe";
		String expectedAffectedText = "alex, a thing occurred with /u/johndoe - he defaulted on a loan with paul at http://reddit.com";
		String expectedReminderTitle = "yo paul do a thing due to johndoe";
		String expectedReminderText = "the incident with /u/johndoe means you need to do a thing";
		
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_title", reminderPMTitleFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_text", reminderPMTextFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_title", affectedUserPMTitleFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_text", affectedUserPMTextFormat, now, now));
		
		
		User alex = database.getUserMapping().fetchOrCreateByName("alex");
		User johndoe = database.getUserMapping().fetchOrCreateByName("johndoe");
		/*User paul = */database.getUserMapping().fetchOrCreateByName("paul");

		Loan loanAlexToJohnDoe = new Loan(
				-1, 		/* loanId */ 			alex.id, 		/* lenderId */
				johndoe.id, /* borrowerId*/			100 * 100, 		/* principal */
				0,			/* princ. repay */		false,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanAlexToJohnDoe);
		
		
		// recall this sets url to http://reddit.com
		Comment comment = SummonTestUtils.createComment("$unpaid /u/johndoe", "paul", "linkauthor");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
		
		List<PMResponse> pmResponses = response.getPMResponses();
		assertNotNull(pmResponses);
		assertEquals(2, pmResponses.size());
		
		PMResponse affectedPM = pmResponses.get(0);
		PMResponse reminderPM = pmResponses.get(1);
		if(!affectedPM.getTo().equals("alex")) {
			affectedPM = pmResponses.get(1);
			reminderPM = pmResponses.get(0);
		}
		
		assertEquals("alex", affectedPM.getTo());
		assertEquals(expectedAffectedTitle, affectedPM.getTitle());
		assertEquals(expectedAffectedText, affectedPM.getText());
		
		assertEquals("paul", reminderPM.getTo());
		assertEquals(expectedReminderTitle, reminderPM.getTitle());
		assertEquals(expectedReminderText, reminderPM.getText());
	}
	
	@Test
	public void testReplacesAuthor() {
		String unpaidFormat = "Sorry about /u/<author> =(";
		String expectedResponse = "Sorry about /u/paul =(";
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_title", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_text", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_title", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_text", "not testing this", now, now));
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/johndoe", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
	}
	
	@Test
	public void testHandlesOneLoan() {
		/*
		 * paul has a loan out with john when he posts an $unpaid message.
		 * the loan is flagged unpaid, john is banned, paul is reminded,
		 * and nobody is alerted
		 */
		final String unpaidFormat = "<changed loans>";
		final String banMessageFormat = "Sucks to be you";
		final String banReasonFormat = "unpaid loan";
		final String banNoteFormat = "he had to go";
		final String affectedLenderPMTitleFormat = "whoaa a thing affected you";
		final String affectedLenderPMTextFormat = "that /u/<borrower> guy is no good";
		final String reminderPMTitleFormat = "yo do a thing";
		final String reminderPMTextFormat = "due to the default from /u/<borrower> you should do a thing";
		
		String expectedResponse = "Lender|Borrower|Amount Given|Amount Repaid|Unpaid?|Original Thread|Date Given|Date Paid Back\n"
				+ ":--|:--|:--|:--|:--|:--|:--|:--\n"
				+ "paul|john|100.00|0.00|***UNPAID***||" + BotUtils.getDateStringFromJUTC(now.getTime()) + "|\n";
		String expectedBanMessage = banMessageFormat;
		String expectedBanReason = banReasonFormat;
		String expectedBanNote = banNoteFormat;
		String expectedReminderPMTitle = reminderPMTitleFormat;
		String expectedReminderPMText = "due to the default from /u/john you should do a thing";
		
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_ban_message", banMessageFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_ban_reason", banReasonFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_ban_note", banNoteFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_title", reminderPMTitleFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_text", reminderPMTextFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_title", affectedLenderPMTitleFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_text", affectedLenderPMTextFormat, now, now));
		
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loanPaulToJohn = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				john.id, 	/* borrowerId*/			100 * 100, 		/* principal */
				0,			/* princ. repay */		false,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToJohn);
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/john", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
		assertTrue(response.shouldBanUser());
		assertEquals("john", response.getUsernameToBan());
		assertEquals(expectedBanMessage, response.getBanMessage());
		assertEquals(expectedBanReason, response.getBanReason());
		assertEquals(expectedBanNote, response.getBanNote());
		assertNotNull(response.getPMResponses());
		assertEquals(1, response.getPMResponses().size());
		PMResponse pmResponse = response.getPMResponses().get(0);
		assertEquals("paul", pmResponse.getTo());
		assertEquals(expectedReminderPMTitle, pmResponse.getTitle());
		assertEquals(expectedReminderPMText, pmResponse.getText());
		assertTrue(response.shouldUnbanUser());
		assertEquals("john", response.getUsernameToUnban());
		
		Loan fromDb = database.getLoanMapping().fetchAll().get(0);
		assertEquals(loanPaulToJohn.id, fromDb.id);
		assertEquals(true, fromDb.unpaid);
	}
	
	@Test
	public void testDoesntRequireLeadingSlash() {
		String unpaidFormat = "Sorry about that =(";
		String expectedResponse = unpaidFormat;
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_title", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_text", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_title", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_text", "not testing this", now, now));
		
		Comment comment = SummonTestUtils.createComment("$unpaid u/johndoe", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
	}
	
	@Test
	public void testDoesntHandleDeletedLoans() {
		/*
		 * There exists only a deleted loan between john and paul, and paul does
		 * an unpaid command on john.
		 * 
		 * john does not get banned, no loans change, no lenders are sent affected pms,
		 * but paul is reminded
		 */
		final String reminderPMTitleFormat = "yo do a thing";
		final String reminderPMTextFormat = "due to the default from /u/<borrower> you should do a thing";

		String expectedReminderPMTitle = reminderPMTitleFormat;
		String expectedReminderPMText = "due to the default from /u/john you should do a thing";
		
		String unpaidFormat = "<changed loans>";
		String expectedResponse = "Lender|Borrower|Amount Given|Amount Repaid|Unpaid?|Original Thread|Date Given|Date Paid Back\n"
				+ ":--|:--|:--|:--|:--|:--|:--|:--\n";
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_title", reminderPMTitleFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_reminder_pm_text", reminderPMTextFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_title", "not testing this", now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_affected_lender_alert_pm_text", "not testing this", now, now));
		
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loanPaulToJohn = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				john.id, 	/* borrowerId*/			100 * 100, 		/* principal */
				0,			/* princ. repay */		false,			/* unpaid */
				true,		/* deleted */			"test",			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToJohn);
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/john", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
		assertFalse(response.shouldBanUser());
		assertFalse(response.shouldUnbanUser());
		assertNotNull(response.getPMResponses());
		List<PMResponse> pmResponses = response.getPMResponses();
		assertEquals(1, pmResponses.size());
		assertEquals("paul", pmResponses.get(0).getTo());
		assertEquals(expectedReminderPMTitle, pmResponses.get(0).getTitle());
		assertEquals(expectedReminderPMText, pmResponses.get(0).getText());
		Loan fromDb = database.getLoanMapping().fetchAll().get(0);
		assertEquals(loanPaulToJohn.id, fromDb.id);
		assertEquals(false, fromDb.unpaid);
	}
	
	@Test
	public void testDoesntRespondToSelf() {
		database.getResponseMapping().save(new Response(-1, "unpaid", "<changed loans>", now, now));
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/john", "LoansBot");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
}
