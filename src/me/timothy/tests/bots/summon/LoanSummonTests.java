package me.timothy.tests.bots.summon;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.BotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.Response;
import me.timothy.bots.summon.SummonResponse;
import me.timothy.bots.summon.loan.LoanSummon;
import me.timothy.jreddit.info.Comment;

/**
 * Describes tests focused on LoanSummon
 * 
 * @author Timothy
 */
public class LoanSummonTests {
	private LoanSummon summon;
	private LoansDatabase database;
	private LoansFileConfiguration config;
	private Timestamp now;
	
	@Before
	public void setUp() throws Exception {
		summon = new LoanSummon();
		database = SummonTestUtils.getTestDatabase();
		config = SummonTestUtils.getTestConfig();
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
		assertNotNull(now);
	}
	
	@Test
	public void testDoesntRespondToSelf() {
		Comment comment = SummonTestUtils.createComment("$loan 500", config.getProperty("user.username"));
		SummonResponse response = summon.handleComment(comment, database, config);
		assertNull(response);
	}
	
	@Test
	public void testRespondsToCorrectNoConversion() {
		database.getResponseMapping().save(new Response(-1, "successful_loan", "<author> -> <link_author> <money1>", now, now));
		final Function<Object[], String> mapToExpected = args -> {
			return args[0] + " -> " + args[1] + " $" + BotUtils.getCostString((Double) args[2]);
		};
		
		final Function<Object[], Void> testFn = args -> {
			String msg = (String) args[0];
			String author = (String) args[1];
			String linkAuthor = (String) args[2];
			Double money = (Double) args[3];
			
			Comment comment = SummonTestUtils.createComment(msg, author, linkAuthor);
			SummonResponse response = summon.handleComment(comment, database, config);
			assertNotNull(response);
			assertEquals(SummonResponse.ResponseType.VALID, response.getResponseType());
			assertEquals(mapToExpected.apply(new Object[] { author, linkAuthor, money }), response.getResponseMessage());
			
			return null;
		};
		
		// Tests are in the form: greg posted a REQ, paul responded
		
		testFn.apply(new Object[] { "$loan 500", "paul", "greg", new Double(500) });
		testFn.apply(new Object[] { "$loan 500.12", "paul", "greg", new Double (500.12) });
		testFn.apply(new Object[] { "some text $loan 500 some text", "paul", "greg", new Double(500) });
		testFn.apply(new Object[] { "some text $loan 500.12 some text", "paul", "greg", new Double (500.12) });
		testFn.apply(new Object[] { "$loan $500", "paul", "greg", new Double(500) });
		testFn.apply(new Object[] { "$loan 500$", "paul", "greg", new Double(500) });
		testFn.apply(new Object[] { "$loan 500.00$", "paul", "greg", new Double(500) });
	}

}
