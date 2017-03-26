package me.timothy.tests.bots.summon;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.BotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.User;
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
		String unpaidFormat = "Sorry about that =(";
		String expectedResponse = unpaidFormat;
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_pm", "asdf", now, now)); // TODO
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/johndoe", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
	}
	
	@Test
	public void testReplacesUser() {
		String unpaidFormat = "Sorry about /u/<user1> =(";
		String expectedResponse = "Sorry about /u/johndoe =(";
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_pm", "asdf", now, now)); // TODO
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/johndoe", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
	}
	
	@Test
	public void testReplacesAuthor() {
		String unpaidFormat = "Sorry about /u/<author> =(";
		String expectedResponse = "Sorry about /u/paul =(";
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_pm", "asdf", now, now)); // TODO
		
		Comment comment = SummonTestUtils.createComment("$unpaid /u/johndoe", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(expectedResponse, response.getResponseMessage());
	}
	
	@Test
	public void testHandlesOneLoan() {
		String unpaidFormat = "<changed loans>";
		String banMessageFormat = "Sucks to be you";
		String banReasonFormat = "unpaid loan";
		String banNoteFormat = "he had to go";
		
		String expectedResponse = "Lender|Borrower|Amount Given|Amount Repaid|Unpaid?|Original Thread|Date Given|Date Paid Back\n"
				+ ":--|:--|:--|:--|:--|:--|:--|:--\n"
				+ "paul|john|100.00|0.00|***UNPAID***||" + BotUtils.getDateStringFromJUTC(now.getTime()) + "|\n";
		String expectedBanMessage = banMessageFormat;
		String expectedBanReason = banReasonFormat;
		String expectedBanNote = banNoteFormat;
		
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_ban_message", banMessageFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_ban_reason", banReasonFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_ban_note", banNoteFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_pm", "asdf", now, now)); // TODO
		
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
		assertFalse(response.shouldUnbanUser());
		
		Loan fromDb = database.getLoanMapping().fetchAll().get(0);
		assertEquals(loanPaulToJohn.id, fromDb.id);
		assertEquals(true, fromDb.unpaid);
	}
	
	@Test
	public void testDoesntHandleDeletedLoans() {
		String unpaidFormat = "<changed loans>";
		String expectedResponse = "Lender|Borrower|Amount Given|Amount Repaid|Unpaid?|Original Thread|Date Given|Date Paid Back\n"
				+ ":--|:--|:--|:--|:--|:--|:--|:--\n";
		database.getResponseMapping().save(new Response(-1, "unpaid", unpaidFormat, now, now));
		database.getResponseMapping().save(new Response(-1, "unpaid_lender_pm", "asdf", now, now)); // TODO
		
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
