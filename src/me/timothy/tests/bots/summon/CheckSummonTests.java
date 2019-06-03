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
import me.timothy.bots.models.ResponseOptOut;
import me.timothy.bots.models.User;
import me.timothy.bots.summon.CheckSummon;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;

/**
 * Describes tests aimed at verifying that CheckSummon 
 * works as expected.
 * 
 * @author Timothy
 */
public class CheckSummonTests {
	private CheckSummon summon;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	private Timestamp now;
	
	@Before 
	public void setUp() throws Exception {
		summon = new CheckSummon();
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
	public void testRespondsToNonMetaLinks() {
		database.getResponseMapping().save(new Response(-1, "check", "test check body", now, now));
		Link link = SummonTestUtils.createLinkByTitle("[REQ] $100 for gas Maine, US");
		
		SummonResponse response = summon.handleLink(link, database, config);
		assertNotNull(response);
	}
	
	@Test
	public void testDoesntRespondToMetaLinks() {
		Link link = SummonTestUtils.createLinkByTitle("[META] LoansBot update queued");
		
		assertFalse(summon.mightInteractWith(link, database, config));
	}
	
	@Test
	public void testIgnoresOptOutForReqs() {
		database.getResponseMapping().save(new Response(-1, "check", "test check body", now, now));
		User john = database.getUserMapping().fetchOrCreateByName("john");
		database.getResponseOptOutMapping().save(new ResponseOptOut(-1, john.id, new Timestamp(System.currentTimeMillis())));
		Link link = SummonTestUtils.createLinkByTitleAndAuthor("[REQ] $100 for gas Maine, US", "john");

		SummonResponse response = summon.handleLink(link, database, config);
		assertNotNull(response);
	}
	
	@Test
	public void testRespectsOptOut() {
		User john = database.getUserMapping().fetchOrCreateByName("john");
		database.getResponseOptOutMapping().save(new ResponseOptOut(-1, john.id, new Timestamp(System.currentTimeMillis())));
		Link link = SummonTestUtils.createLinkByTitleAndAuthor("[PAID] $100 from paul", "john");

		assertFalse(summon.mightInteractWith(link, database, config));
	}
	
	@Test
	public void testRespondsToCheckComment() {
		database.getResponseMapping().save(new Response(-1, "check", "test check body", now, now));
		Comment comment = SummonTestUtils.createComment("$check /u/john", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
	}
	
	@Test
	public void testRespondsWithoutLeadingSlashOnUsername() {
		database.getResponseMapping().save(new Response(-1, "check", "test check body", now, now));
		Comment comment = SummonTestUtils.createComment("$check u/john", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
	}
	
	@Test
	public void testRespondsToLinkedUsername() {
		database.getResponseMapping().save(new Response(-1, "check", "test check body", now, now));
		Comment comment = SummonTestUtils.createComment("$check [john](https://reddit.com/user/john)", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
	}
	
	@Test
	public void testRespondsToEscapedUnderscore() {
		database.getResponseMapping().save(new Response(-1, "check", "test check body", now, now));
		Comment comment = SummonTestUtils.createComment("$check [/u/john\\_doe](https://reddit.com/user/john_doe)", "john_doe");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		
		comment = SummonTestUtils.createComment("$check /u/alfred\\_the\\_third", "alfred_the_third");
		response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		
		comment = SummonTestUtils.createComment("$check u/three\\_\\_two", "three__two");
		response = summon.handleComment(comment, database, config);
		assertNotNull(response);
	}
	
	@Test
	public void testDoesntRespondToBadEscapedUnderscore() {
		database.getResponseMapping().save(new Response(-1, "check", "test check body", now, now));
		Comment comment = SummonTestUtils.createComment("$check [/u/john\\_doe](https://reddit.com/user/john\\_doe)", "john_doe");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);


		comment = SummonTestUtils.createComment("$check [/u/pink_](https://reddit.com/user/pink\\_)", "pink_"); // cannot escape in link
		response = summon.handleComment(comment, database, config);
		assertNull(response);
		
		comment = SummonTestUtils.createComment("$check u\\_/three\\_\\_two", "three__two");
		response = summon.handleComment(comment, database, config);
		assertNull(response);

		comment = SummonTestUtils.createComment("$check u/\\three", "three");
		response = summon.handleComment(comment, database, config);
		assertNull(response);

		comment = SummonTestUtils.createComment("$check u/th\\ree", "three");
		response = summon.handleComment(comment, database, config);
		assertNull(response);
		
		comment = SummonTestUtils.createComment("$check u/\\three_\\test", "three");
		response = summon.handleComment(comment, database, config);
		assertNull(response);

		comment = SummonTestUtils.createComment("$check u\\/three", "three");
		response = summon.handleComment(comment, database, config);
		assertNull(response);
		

		comment = SummonTestUtils.createComment("$check \\u/_three", "three");
		response = summon.handleComment(comment, database, config);
		assertNull(response);

		comment = SummonTestUtils.createComment("$check u\\/_three", "three");
		response = summon.handleComment(comment, database, config);
		assertNull(response);

		comment = SummonTestUtils.createComment("$check \\/u/_three", "three");
		response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	@Test
	public void testDoesntRespondToSelfComment() {
		Comment comment = SummonTestUtils.createComment("$check /u/john", "LoansBot");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	@Test
	public void testDoesntRespondToRandomComment() {
		Comment comment = SummonTestUtils.createComment("you should do a $check here", "john");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	@Test
	public void testRespondsWithCheckResponseForComment() {
		String checkResponse = "test check response";
		database.getResponseMapping().save(new Response(-1, "check", checkResponse, now, now));
		
		Comment comment = SummonTestUtils.createComment("$check /u/john", "john");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponse, response.getResponseMessage());
	}
	
	@Test
	public void testRespondsWithCheckResponseForLink() {
		String checkResponse = "test check response";
		database.getResponseMapping().save(new Response(-1, "check", checkResponse, now, now));
		
		Link link = SummonTestUtils.createLinkByTitle("[REQ] $100 for gas, Boston, MA");
		
		SummonResponse response = summon.handleLink(link, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponse, response.getResponseMessage());
	}
	
	@Test
	public void testCanSubstituteUserInComment() {
		String checkResponseFormat = "test check response for <user1>";
		String user = "paul";
		String checkResponseExpected = "test check response for " + user;
		database.getResponseMapping().save(new Response(-1, "check", checkResponseFormat, now, now));
		
		Comment comment = SummonTestUtils.createComment("$check /u/" + user, "john");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponseExpected, response.getResponseMessage());
	}
	
	@Test
	public void testCanSubstituteAuthorInComment() {
		String checkResponseFormat = "test check response for <author>";
		String author = "john";
		String checkResponseExpected = "test check response for " + author;
		database.getResponseMapping().save(new Response(-1, "check", checkResponseFormat, now, now));
		
		Comment comment = SummonTestUtils.createComment("$check /u/paul", author);
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponseExpected, response.getResponseMessage());
	}
	
	@Test
	public void testCanSubstituteUserInLink() {
		String checkResponseFormat = "test check response for <user1>";
		String user = "paul";
		String checkResponseExpected = "test check response for " + user;
		database.getResponseMapping().save(new Response(-1, "check", checkResponseFormat, now, now));
		
		Link link = SummonTestUtils.createLinkByTitleAndAuthor("[REQ] $100 for gas, Fairville, Magic", user);
		
		SummonResponse response = summon.handleLink(link, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponseExpected, response.getResponseMessage());
	}
	
	@Test
	public void testCanSubstituteAuthorInLink() {
		String checkResponseFormat = "test check response for <author>";
		String user = "paul";
		String checkResponseExpected = "test check response for " + user;
		database.getResponseMapping().save(new Response(-1, "check", checkResponseFormat, now, now));
		
		Link link = SummonTestUtils.createLinkByTitleAndAuthor("[REQ] $100 for gas, Fairville, Magic", user);
		
		SummonResponse response = summon.handleLink(link, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponseExpected, response.getResponseMessage());
	}
	
	@Test
	public void testCanSubstituteLoansIfNoHistoryInComment() {
		String checkResponseFormat = "<loans1>";
		String checkResponseExpected = "No History\n\n";
		database.getResponseMapping().save(new Response(-1, "check", checkResponseFormat, now, now));
		
		Comment comment = SummonTestUtils.createComment("$check /u/john", "paul");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponseExpected, response.getResponseMessage());
	}
	
	@Test
	public void testCanSubstituteLoansWithHistoryInComment() {
		String checkResponseFormat = "<loans1>";
		String checkResponseExpected = 
				  "Lender|Borrower|Amount Given|Amount Repaid|Unpaid?|Original Thread|Date Given|Date Paid Back\n"
				+ ":--|:--|:--|:--|:--|:--|:--|:--\n"
				+ "paul|john|100.00|0.00|||" + BotUtils.getDateStringFromJUTC(now.getTime()) + "|\n";
		database.getResponseMapping().save(new Response(-1, "check", checkResponseFormat, now, now));
		
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		Loan loanPaulToJohn = new Loan(-1, paul.id, john.id, 100 * 100, 0, false, false, null, now, now, null);
		database.getLoanMapping().save(loanPaulToJohn);
		
		Comment comment = SummonTestUtils.createComment("$check /u/paul", "greg");
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponseExpected, response.getResponseMessage());
	}
	
	@Test
	public void testDoesntIncludeDeletedLoansWithHistoryInComment() {
		String checkResponseFormat = "<loans1>";
		String checkResponseExpected = "No History\n\n";
		database.getResponseMapping().save(new Response(-1, "check", checkResponseFormat, now, now));
		
		Comment comment = SummonTestUtils.createComment("$check /u/john", "paul");
		
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		Loan loanPaulToJohn = new Loan(-1, paul.id, john.id, 100 * 100, 0, false, true, "test", now, now, now);
		database.getLoanMapping().save(loanPaulToJohn);
		
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals(checkResponseExpected, response.getResponseMessage());
	}
}
