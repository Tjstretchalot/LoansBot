package me.timothy.tests.bots.summon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.User;
import me.timothy.bots.summon.ConfirmSummon;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.jreddit.info.Comment;

/**
 * Describes tests focused on ConfirmSummon.
 * 
 * @author Timothy
 */
public class ConfirmSummonTests {
	private ConfirmSummon summon;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	private Timestamp now;
	
	@Before 
	public void setUp() throws Exception {
		summon = new ConfirmSummon();
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
	public void testDoesntRespondToMiscComment() {
		Comment comment = SummonTestUtils.createComment("here i am $confirming stuff", "john");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	@Test
	public void testDoesntRespondToSelf() {
		Comment comment = SummonTestUtils.createComment("$confirm /u/john 100", "LoansBot");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}

	@Test
	public void testRespondsWhenNoLeadingSlash() {
		database.getResponseMapping().save(new Response(-1, "confirmNoLoan", "has loan", now, now));
		Comment comment = SummonTestUtils.createComment("$confirm u/john 100", "other");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
	}
	
	@Test
	public void testRespondsToCommentWithLoan() {
		database.getResponseMapping().save(new Response(-1, "confirm", "has loan", now, now));
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		Loan loanPaulToGreg = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				greg.id, 	/* borrowerId*/			100 * 100, 		/* principal */
				0,			/* princ. repay */		false,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToGreg);
		
		Comment comment = SummonTestUtils.createComment("some stuff $confirm /u/paul $100 more stuff", "greg");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("has loan", response.getResponseMessage());
	}
	
	@Test
	public void testResponseToConfirmNoLoanWithConversion() throws Exception {
		SummonTestUtils.overrideCurrencyConversion("GDP", "USD", 1.15);
		database.getResponseMapping().save(new Response(-1, "confirmNoLoanHasConversion", "<money1>", now, now));
		
		Comment comment = SummonTestUtils.createComment("some stuff $confirm /u/paul 100 GDP more stuff", "greg");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("$115.00", response.getResponseMessage());
	}
	
	@Test
	public void testResponseToConfirmWithConversion() throws Exception {
		SummonTestUtils.overrideCurrencyConversion("GDP", "USD", 1.29);
		database.getResponseMapping().save(new Response(-1, "confirmHasConversion", "<money1>", now, now));
		
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		Loan loanPaulToGreg = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				greg.id, 	/* borrowerId*/			129 * 100, 		/* principal */
				0,			/* princ. repay */		false,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToGreg);
		
		Comment comment = SummonTestUtils.createComment("$confirm /u/paul 100 GDP", "greg");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("$129.00", response.getResponseMessage());
	}
	
	@Test
	public void testResponseToNumberWithComma() throws Exception {
		database.getResponseMapping().save(new Response(-1, "confirmNoLoan", "<money1>", now, now));
		
		Comment comment = SummonTestUtils.createComment("$confirm /u/paul $1,000.00", "greg");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("$1,000.00", response.getResponseMessage());
	}
	
	@Test
	public void testDoesntRespondToMalformedNumberWithComma() throws Exception {
		database.getResponseMapping().save(new Response(-1, "confirmNoLoan", "<money1>", now, now));
		
		Comment comment = SummonTestUtils.createComment("$confirm /u/paul $50,00", "greg");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
		
		comment = SummonTestUtils.createComment("$confirm /u/paul $10,00.00", "greg");
		response = summon.handleComment(comment, database, config);
		assertNull(response);

		comment = SummonTestUtils.createComment("$confirm /u/paul 33,00 and other stuff", "greg");
		response = summon.handleComment(comment, database, config);
		assertNull(response);

		comment = SummonTestUtils.createComment("asdf $confirm /u/paul 3,3$ asdf", "greg");
		response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
}
