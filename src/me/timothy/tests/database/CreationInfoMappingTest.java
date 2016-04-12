package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.CreationInfo;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;

/**
 * Describes a test focused on testing CreationInfoMapping in a 
 * MappingDatabase. The database must be <i>completely</i> empty
 * prior to the start of each test. The database <i>will</i> be 
 * modified. Do <b>not</b> run this on a production database. 
 */
public class CreationInfoMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}

	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loanPaulToJohn = new Loan();
		loanPaulToJohn.id = -1;
		loanPaulToJohn.lenderId = paul.id;
		loanPaulToJohn.borrowerId = john.id;
		loanPaulToJohn.principalCents = 100 * 100; // $100
		loanPaulToJohn.principalRepaymentCents = 0;
		loanPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn);
		
		CreationInfo cInfoPaulToJohn = new CreationInfo();
		cInfoPaulToJohn.id = -1;
		cInfoPaulToJohn.loanId = loanPaulToJohn.id;
		cInfoPaulToJohn.type = CreationInfo.CreationType.REDDIT;
		cInfoPaulToJohn.thread = "https://www.reddit.com/r/LoansBot/comments/2gp46g/loansbot_now_monitors_rborrow/";
		cInfoPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		cInfoPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getCreationInfoMapping().save(cInfoPaulToJohn);
		assertTrue(cInfoPaulToJohn.id > 0);
		
		List<CreationInfo> fromDb = database.getCreationInfoMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + cInfoPaulToJohn, fromDb.contains(cInfoPaulToJohn));
	}
	
	@Test
	public void testFetchById() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loanPaulToJohn = new Loan();
		loanPaulToJohn.id = -1;
		loanPaulToJohn.lenderId = paul.id;
		loanPaulToJohn.borrowerId = john.id;
		loanPaulToJohn.principalCents = 100 * 100; // $100
		loanPaulToJohn.principalRepaymentCents = 0;
		loanPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn);
		
		CreationInfo cInfoPaulToJohn = new CreationInfo();
		cInfoPaulToJohn.id = -1;
		cInfoPaulToJohn.loanId = loanPaulToJohn.id;
		cInfoPaulToJohn.type = CreationInfo.CreationType.REDDIT;
		cInfoPaulToJohn.thread = "https://www.reddit.com/r/LoansBot/comments/2gp46g/loansbot_now_monitors_rborrow/";
		cInfoPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		cInfoPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getCreationInfoMapping().save(cInfoPaulToJohn);
		
		CreationInfo fromDb = database.getCreationInfoMapping().fetchById(cInfoPaulToJohn.id);
		assertEquals(cInfoPaulToJohn, fromDb);
	}
	
	@Test
	public void testFetchByLoanId() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loanPaulToJohn = new Loan();
		loanPaulToJohn.id = -1;
		loanPaulToJohn.lenderId = paul.id;
		loanPaulToJohn.borrowerId = john.id;
		loanPaulToJohn.principalCents = 100 * 100; // $100
		loanPaulToJohn.principalRepaymentCents = 0;
		loanPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn);
		
		CreationInfo cInfoPaulToJohn = new CreationInfo();
		cInfoPaulToJohn.id = -1;
		cInfoPaulToJohn.loanId = loanPaulToJohn.id;
		cInfoPaulToJohn.type = CreationInfo.CreationType.REDDIT;
		cInfoPaulToJohn.thread = "https://www.reddit.com/r/LoansBot/comments/2gp46g/loansbot_now_monitors_rborrow/";
		cInfoPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		cInfoPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getCreationInfoMapping().save(cInfoPaulToJohn);
		
		CreationInfo fromDb = database.getCreationInfoMapping().fetchByLoanId(loanPaulToJohn.id);
		assertEquals(cInfoPaulToJohn, fromDb);
		
		Loan loanPaulToJohn2 = new Loan();
		loanPaulToJohn2.id = -1;
		loanPaulToJohn2.lenderId = paul.id;
		loanPaulToJohn2.borrowerId = john.id;
		loanPaulToJohn2.principalCents = 50 * 100; // $50
		loanPaulToJohn2.principalRepaymentCents = 0;
		loanPaulToJohn2.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn2);
		
		CreationInfo cInfoPaulToJohn2 = new CreationInfo();
		cInfoPaulToJohn2.id = -1;
		cInfoPaulToJohn2.loanId = loanPaulToJohn2.id;
		cInfoPaulToJohn2.type = CreationInfo.CreationType.REDDITLOANS;
		cInfoPaulToJohn2.reason = "the loansbot missed it and stuff";
		cInfoPaulToJohn2.userId = paul.id;
		cInfoPaulToJohn2.createdAt = new Timestamp(System.currentTimeMillis());
		cInfoPaulToJohn2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getCreationInfoMapping().save(cInfoPaulToJohn2);
		
		fromDb = database.getCreationInfoMapping().fetchByLoanId(loanPaulToJohn2.id);
		assertEquals(cInfoPaulToJohn2, fromDb);
	}
	
	@Test
	public void testFetchManyByLoanIds() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loanPaulToJohn = new Loan();
		loanPaulToJohn.id = -1;
		loanPaulToJohn.lenderId = paul.id;
		loanPaulToJohn.borrowerId = john.id;
		loanPaulToJohn.principalCents = 50 * 100; // $50
		loanPaulToJohn.principalRepaymentCents = 0;
		loanPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn);;
		
		CreationInfo cInfoPaulToJohn = new CreationInfo();
		cInfoPaulToJohn.id = -1;
		cInfoPaulToJohn.loanId = loanPaulToJohn.id;
		cInfoPaulToJohn.type = CreationInfo.CreationType.REDDIT;
		cInfoPaulToJohn.thread = "https://www.reddit.com/r/LoansBot/comments/2gp46g/loansbot_now_monitors_rborrow/";
		cInfoPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		cInfoPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getCreationInfoMapping().save(cInfoPaulToJohn);
		
		Loan loanPaulToJohn2 = new Loan();
		loanPaulToJohn2.id = -1;
		loanPaulToJohn2.lenderId = paul.id;
		loanPaulToJohn2.borrowerId = john.id;
		loanPaulToJohn2.principalCents = 50 * 100; // $50
		loanPaulToJohn2.principalRepaymentCents = 0;
		loanPaulToJohn2.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn2);
		
		CreationInfo cInfoPaulToJohn2 = new CreationInfo();
		cInfoPaulToJohn2.id = -1;
		cInfoPaulToJohn2.loanId = loanPaulToJohn2.id;
		cInfoPaulToJohn2.type = CreationInfo.CreationType.REDDITLOANS;
		cInfoPaulToJohn2.reason = "the loansbot missed it and stuff";
		cInfoPaulToJohn2.userId = paul.id;
		cInfoPaulToJohn2.createdAt = new Timestamp(System.currentTimeMillis());
		cInfoPaulToJohn2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getCreationInfoMapping().save(cInfoPaulToJohn2);
		
		List<CreationInfo> fromDb = database.getCreationInfoMapping().fetchManyByLoanIds(loanPaulToJohn.id);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + cInfoPaulToJohn, fromDb.contains(cInfoPaulToJohn));
		
		fromDb = database.getCreationInfoMapping().fetchManyByLoanIds(loanPaulToJohn2.id);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + cInfoPaulToJohn2, fromDb.contains(cInfoPaulToJohn2));
		
		fromDb = database.getCreationInfoMapping().fetchManyByLoanIds(loanPaulToJohn2.id, loanPaulToJohn.id);
		assertEquals(2, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + cInfoPaulToJohn, fromDb.contains(cInfoPaulToJohn));
		assertTrue("expected " + fromDb + " to contain " + cInfoPaulToJohn2, fromDb.contains(cInfoPaulToJohn2));
	}
	
	
}
