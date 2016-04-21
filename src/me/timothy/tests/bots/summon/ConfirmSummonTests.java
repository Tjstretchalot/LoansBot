package me.timothy.tests.bots.summon;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
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
	
	@Before 
	public void setUp() throws Exception {
		summon = new ConfirmSummon();
		database = SummonTestUtils.getTestDatabase();
		config = SummonTestUtils.getTestConfig();
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
}
