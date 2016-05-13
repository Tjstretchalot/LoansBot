package me.timothy.tests.bots.summon;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.Response;
import me.timothy.bots.summon.BadLoanSummon;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.jreddit.info.Comment;

/**
 * Tests aimed at ensuring BadLoanSummon works
 * as expected
 * 
 * @author Timothy
 * @see me.timothy.bots.summon.BadLoanSummon
 */
public class BadLoanSummonTests {
	private BadLoanSummon summon;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	private Timestamp now;
	
	@Before
	public void setUp() throws NullPointerException, IOException {
		summon = new BadLoanSummon();
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
	public void testDoesNotReplyToValidSummon() {
		Comment comment = SummonTestUtils.createComment("$loan 100", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
		
		comment = SummonTestUtils.createComment("$loan $100.00", "paul");		
		response = summon.handleComment(comment, database, config);
		assertNull(response);
		
		comment = SummonTestUtils.createComment("$loan 100 EUR", "paul");
		response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	@Test
	public void testDoesReplyToUsernameAdded() {
		database.getResponseMapping().save(new Response(-1, "bad_loan_summon", "bad loan", now, now));
		Comment comment = SummonTestUtils.createComment("$loan /u/john 50", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.INVALID, response.getResponseType());
		assertEquals("bad loan", response.getResponseMessage());
		
		comment = SummonTestUtils.createComment("$loan /u/john 50 EUR", "paul");
		response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.INVALID, response.getResponseType());
		assertEquals("bad loan", response.getResponseMessage());
		
		comment = SummonTestUtils.createComment("$loan /u/asdf $50 more stuff here", "paul");
		response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.INVALID, response.getResponseType());
		assertEquals("bad loan", response.getResponseMessage());
	}
	
	@Test
	public void testHandlesCommas() {
		database.getResponseMapping().save(new Response(-1, "bad_loan_summon", "bad loan", now, now));
		Comment comment = SummonTestUtils.createComment("$loan /u/john $5,000.00", "paul");
		SummonResponse response = summon.handleComment(comment,  database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.INVALID, response.getResponseType());
		assertEquals("bad loan", response.getResponseMessage());
		
		comment = SummonTestUtils.createComment("$loan /u/john $5.001", "paul");
		response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
}
