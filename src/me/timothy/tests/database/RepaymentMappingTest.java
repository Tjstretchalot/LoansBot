package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Repayment;
import me.timothy.bots.models.User;

/**
 * Describes a test that focuses on testing a RepaymentMapping inside
 * a MappingDatabase. The database should be cleared prior to each test.
 * The database will be modified in each test. Do not run against a 
 * production database.
 * 
 * @author Timothy
 */
public class RepaymentMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test 
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		Loan loanPaulToGreg = new Loan();
		loanPaulToGreg.id = -1;
		loanPaulToGreg.borrowerId = greg.id;
		loanPaulToGreg.lenderId = paul.id;
		loanPaulToGreg.principalCents = 100 * 100; // $100
		loanPaulToGreg.principalRepaymentCents = 0;
		loanPaulToGreg.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToGreg.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToGreg);
		
		Repayment repayment = new Repayment();
		repayment.id = -1;
		repayment.amountCents = 100 * 100; // $100
		repayment.loanId = loanPaulToGreg.id;
		repayment.createdAt = new Timestamp(System.currentTimeMillis());
		repayment.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getRepaymentMapping().save(repayment);
		
		loanPaulToGreg.principalRepaymentCents = 100 * 100; // $100
		database.getLoanMapping().save(loanPaulToGreg);
		
		List<Repayment> fromDb = database.getRepaymentMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + repayment, fromDb.contains(repayment));
	}
	
	@Test
	public void testFetchByLoanId() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loanPaulToGreg = new Loan();
		loanPaulToGreg.id = -1;
		loanPaulToGreg.borrowerId = greg.id;
		loanPaulToGreg.lenderId = paul.id;
		loanPaulToGreg.principalCents = 100 * 100; // $100
		loanPaulToGreg.principalRepaymentCents = 0;
		loanPaulToGreg.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToGreg.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToGreg);
		
		Loan loanPaulToJohn = new Loan();
		loanPaulToJohn.id = -1;
		loanPaulToJohn.borrowerId = john.id;
		loanPaulToJohn.lenderId = paul.id;
		loanPaulToJohn.principalCents = 50 * 100; // $50
		loanPaulToJohn.principalRepaymentCents = 0;
		loanPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn);
		
		Repayment repaymentPaulToGreg = new Repayment();
		repaymentPaulToGreg.id = -1;
		repaymentPaulToGreg.amountCents = 100 * 100; // $100
		repaymentPaulToGreg.loanId = loanPaulToGreg.id;
		repaymentPaulToGreg.createdAt = new Timestamp(System.currentTimeMillis());
		repaymentPaulToGreg.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getRepaymentMapping().save(repaymentPaulToGreg);
		
		loanPaulToGreg.principalRepaymentCents = 100 * 100; // $100
		database.getLoanMapping().save(loanPaulToGreg);
		
		List<Repayment> fromDb = database.getRepaymentMapping().fetchByLoanId(loanPaulToGreg.id);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + repaymentPaulToGreg, fromDb.contains(repaymentPaulToGreg));
		
		fromDb = database.getRepaymentMapping().fetchByLoanId(loanPaulToJohn.id);
		assertEquals(0, fromDb.size());
		
		Repayment repaymentPaulToJohn = new Repayment();
		repaymentPaulToJohn.id = -1;
		repaymentPaulToJohn.amountCents = 50 * 100; // $50
		repaymentPaulToJohn.loanId = loanPaulToJohn.id;
		repaymentPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		repaymentPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getRepaymentMapping().save(repaymentPaulToJohn);
		
		loanPaulToJohn.principalRepaymentCents = 50 * 100; // $50
		database.getLoanMapping().save(loanPaulToJohn);
		
		fromDb = database.getRepaymentMapping().fetchByLoanId(loanPaulToJohn.id);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + repaymentPaulToJohn, fromDb.contains(repaymentPaulToJohn));
	}
}
