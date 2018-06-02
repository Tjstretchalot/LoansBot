package me.timothy.tests.bots.summon;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.User;
import me.timothy.bots.summon.PaidSummon;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.jreddit.info.Comment;

/**
 * Describes tests focused on PaidSummon
 * 
 * @author Timothy
 */
public class PaidSummonTests {
	private PaidSummon summon;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	private Timestamp beforeNow;
	private Timestamp now;
	
	@Before
	public void setUp() throws Exception {
		summon = new PaidSummon();
		database = SummonTestUtils.getTestDatabase();
		config = SummonTestUtils.getTestConfig();
		beforeNow = new Timestamp(System.currentTimeMillis() - 10000);
		now = new Timestamp(System.currentTimeMillis());
	}

	@After
	public void tearDown() throws Exception {
		database.disconnect();
	}

	@Test
	public void testTest() {
		assertNotNull(summon);
		assertNotNull(database);
		assertNotNull(config);
		assertNotNull(beforeNow);
		assertNotNull(now);
	}

	@Test
	public void testDoesntRespondToMiscComment() {
		Comment comment = SummonTestUtils.createComment("here i am $paying stuff", "john");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}

	@Test
	public void testDoesntRespondToSelf() {
		Comment comment = SummonTestUtils.createComment("$paid /u/john 100", "LoansBot");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	
	@Test
	public void testRepaysSingleLoan() {
		database.getResponseMapping().save(new Response(-1, "repayment", "has loan", now, now));
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
		
		Comment comment = SummonTestUtils.createComment("some stuff $paid /u/greg $100 more stuff", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("has loan", response.getResponseMessage());
		assertFalse(response.shouldUnbanUser());
		
		List<Loan> fromDB = database.getLoanMapping().fetchAll();
		assertNotNull(fromDB);
		assertEquals(1, fromDB.size());
		
		Loan loanFromDB = fromDB.get(0);
		assertEquals(loanPaulToGreg.id, loanFromDB.id);
		
		assertEquals(100 * 100, loanFromDB.principalRepaymentCents);
	}
	
	@Test
	public void testDoesntRequireLeadingSlashOnUsername() {
		database.getResponseMapping().save(new Response(-1, "repayment", "has loan", now, now));
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		Loan loanPaulToGreg = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				greg.id, 	/* borrowerId*/			10050, 		/* principal */
				0,			/* princ. repay */		false,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToGreg);
		
		Comment comment = SummonTestUtils.createComment("some stuff $paid u/greg $100.50 more stuff", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("has loan", response.getResponseMessage());
		assertFalse(response.shouldUnbanUser());
		
		List<Loan> fromDB = database.getLoanMapping().fetchAll();
		assertNotNull(fromDB);
		assertEquals(1, fromDB.size());
		
		Loan loanFromDB = fromDB.get(0);
		assertEquals(loanPaulToGreg.id, loanFromDB.id);
		
		assertEquals(10050, loanFromDB.principalRepaymentCents);
	}
	
	
	@Test
	public void testUnbansUser()
	{
		database.getResponseMapping().save(new Response(-1, "repayment", "has loan", now, now));
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		Loan loanPaulToGreg = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				greg.id, 	/* borrowerId*/			100 * 100, 		/* principal */
				0,			/* princ. repay */		true,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				beforeNow,	/* created at */		beforeNow,		/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToGreg);
		
		Comment comment = SummonTestUtils.createComment("some stuff $paid /u/greg $100 more stuff", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("has loan", response.getResponseMessage());
		assertTrue(response.shouldUnbanUser());
		assertEquals("greg", response.getUsernameToUnban());
		
		List<Loan> fromDB = database.getLoanMapping().fetchAll();
		assertNotNull(fromDB);
		assertEquals(1, fromDB.size());
		
		Loan loanFromDB = fromDB.get(0);
		assertEquals(loanPaulToGreg.id, loanFromDB.id);
		
		assertEquals(100 * 100, loanFromDB.principalRepaymentCents);
	}
	

	@Test
	public void testDoesntAlwaysUnbansUser()
	{
		database.getResponseMapping().save(new Response(-1, "repayment", "has loan", now, now));
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		Loan loanPaulToGreg = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				greg.id, 	/* borrowerId*/			100 * 100, 		/* principal */
				0,			/* princ. repay */		true,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToGreg);

		Loan loanPaulToGreg2 = new Loan(
				-1, 		/* loanId */ 			paul.id, 		/* lenderId */
				greg.id, 	/* borrowerId*/			100 * 100, 		/* principal */
				0,			/* princ. repay */		true,			/* unpaid */
				false,		/* deleted */			null,			/* deleted reason */
				now,		/* created at */		now,			/* updated at */
				null		/* deleted at */
				);
		database.getLoanMapping().save(loanPaulToGreg2);
		
		Comment comment = SummonTestUtils.createComment("some stuff $paid /u/greg $100 more stuff", "paul");
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNotNull(response);
		assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
		assertEquals("has loan", response.getResponseMessage());
		assertFalse(response.shouldUnbanUser());
		
		List<Loan> fromDB = database.getLoanMapping().fetchAll();
		assertNotNull(fromDB);
		assertEquals(2, fromDB.size());
		
		Loan loanFromDB = fromDB.get(0);
		if(loanFromDB.id != loanPaulToGreg.id)
			loanFromDB = fromDB.get(1);
		
		assertEquals(loanPaulToGreg.id, loanFromDB.id);
		
		assertEquals(100 * 100, loanFromDB.principalRepaymentCents);
	}
}
