package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;

/**
 * Describes a test focused on a LoanMapping in a MappingDatabase. The 
 * database must be <i>completely</i> empty after each setUp. The database
 * <i>will</i> be modified after each run - do not run against real databases.
 * 
 * @author Timothy
 */
public class LoanMappingTest {
	/**
	 * The database to test
	 */
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		
		Loan loan = new Loan();
		loan.id = -1;
		loan.lenderId = paul.id;
		loan.borrowerId = john.id;
		loan.principalCents = 100 * 100; // $100
		loan.principalRepaymentCents = 0;
		loan.createdAt = new Timestamp(System.currentTimeMillis());
		loan.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loan);
		
		assertTrue(loan.id > 0);
		
		List<Loan> fromDb = database.getLoanMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loan, fromDb.contains(loan));
	}
	
	@Test
	public void testFetchLendersIdsWithNewLoanSince() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		Timestamp past = new Timestamp(System.currentTimeMillis() - 60000);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Timestamp future = new Timestamp(System.currentTimeMillis() + 60000);
		
		List<Integer> fromDb = database.getLoanMapping().fetchLenderIdsWithNewLoanSince(past);
		assertEquals(0, fromDb.size());
		
		Loan loanPaulToJohn = new Loan();
		loanPaulToJohn.id = -1;
		loanPaulToJohn.lenderId = paul.id;
		loanPaulToJohn.borrowerId = john.id;
		loanPaulToJohn.principalCents = 100 * 100; // $100
		loanPaulToJohn.principalRepaymentCents = 0;
		loanPaulToJohn.createdAt = new Timestamp(now.getTime());
		loanPaulToJohn.updatedAt = new Timestamp(now.getTime());
		database.getLoanMapping().save(loanPaulToJohn);
		
		fromDb = database.getLoanMapping().fetchLenderIdsWithNewLoanSince(past);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + paul.id, fromDb.contains(paul.id));
		
		fromDb = database.getLoanMapping().fetchLenderIdsWithNewLoanSince(future);
		assertEquals(0, fromDb.size());
		
		Loan loanGregToJohn = new Loan();
		loanGregToJohn.id = -1;
		loanGregToJohn.lenderId = greg.id;
		loanGregToJohn.borrowerId = john.id;
		loanGregToJohn.principalCents = 50 * 100; // $50
		loanGregToJohn.principalRepaymentCents = 0;
		loanGregToJohn.createdAt = new Timestamp(now.getTime());
		loanGregToJohn.updatedAt = new Timestamp(now.getTime());
		database.getLoanMapping().save(loanGregToJohn);
		
		fromDb = database.getLoanMapping().fetchLenderIdsWithNewLoanSince(past);
		assertEquals(2, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + paul.id, fromDb.contains(paul.id));
		assertTrue("expected " + fromDb + " to contain " + greg.id, fromDb.contains(greg.id));
		
		Loan loanGregToJohn2 = new Loan();
		loanGregToJohn2.id = -1;
		loanGregToJohn2.lenderId = greg.id;
		loanGregToJohn2.borrowerId = john.id;
		loanGregToJohn2.principalCents = 50 * 100; // $50
		loanGregToJohn2.principalRepaymentCents = 0;
		loanGregToJohn2.createdAt = new Timestamp(now.getTime());
		loanGregToJohn2.updatedAt = new Timestamp(now.getTime());
		database.getLoanMapping().save(loanGregToJohn2);
		
		fromDb = database.getLoanMapping().fetchLenderIdsWithNewLoanSince(past);
		assertEquals(2, fromDb.size()); // don't duplicate lender ids
		assertTrue("expected " + fromDb + " to contain " + paul.id, fromDb.contains(paul.id));
		assertTrue("expected " + fromDb + " to contain " + greg.id, fromDb.contains(greg.id));
	}
	
	@Test
	public void testFetchWithBorrowerAndOrLender() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		List<Loan> fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(paul.id, greg.id, false);
		assertEquals(0, fromDb.size());
		
		Loan loanPaulToJohn = new Loan();
		loanPaulToJohn.id = -1;
		loanPaulToJohn.lenderId = paul.id;
		loanPaulToJohn.borrowerId = john.id;
		loanPaulToJohn.principalCents = 100 * 100; // $100
		loanPaulToJohn.principalRepaymentCents = 0;
		loanPaulToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn);
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(paul.id, paul.id, false);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loanPaulToJohn, fromDb.contains(loanPaulToJohn));
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, john.id, false);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loanPaulToJohn, fromDb.contains(loanPaulToJohn));
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(paul.id, paul.id, true);
		assertEquals(0, fromDb.size());
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, john.id, true);
		assertEquals(0, fromDb.size());
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, true);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loanPaulToJohn, fromDb.contains(loanPaulToJohn));
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, false);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loanPaulToJohn, fromDb.contains(loanPaulToJohn));
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(paul.id, john.id, false);
		assertEquals(0, fromDb.size());
		
		Loan loanGregToJohn = new Loan();
		loanGregToJohn.id = -1;
		loanGregToJohn.lenderId = greg.id;
		loanGregToJohn.borrowerId = john.id;
		loanGregToJohn.principalCents = 50 * 100; // $50
		loanGregToJohn.principalRepaymentCents = 0;
		loanGregToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanGregToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanGregToJohn);
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, john.id, false);
		assertEquals(2, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loanPaulToJohn, fromDb.contains(loanPaulToJohn));
		assertTrue("expected " + fromDb + " to contain " + loanGregToJohn, fromDb.contains(loanGregToJohn));
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, greg.id, false);
		assertEquals(2, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loanPaulToJohn, fromDb.contains(loanPaulToJohn));
		assertTrue("expected " + fromDb + " to contain " + loanGregToJohn, fromDb.contains(loanGregToJohn));
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, greg.id, true);
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + loanGregToJohn, fromDb.contains(loanGregToJohn));
	}
	
	@Test
	public void testFetchNumberOfLoansWithUserAsLender() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		int fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(john.id);
		assertEquals(0, fromDb);

		Loan loanPaulToJohn1 = new Loan();
		loanPaulToJohn1.id = -1;
		loanPaulToJohn1.lenderId = paul.id;
		loanPaulToJohn1.borrowerId = john.id;
		loanPaulToJohn1.principalCents = 100 * 100; // $100
		loanPaulToJohn1.principalRepaymentCents = 0;
		loanPaulToJohn1.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn1.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn1);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(john.id);
		assertEquals(0, fromDb);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(paul.id);
		assertEquals(1, fromDb);
		
		Loan loanGregToJohn = new Loan();
		loanGregToJohn.id = -1;
		loanGregToJohn.lenderId = greg.id;
		loanGregToJohn.borrowerId = john.id;
		loanGregToJohn.principalCents = 50 * 100; // $50
		loanGregToJohn.principalRepaymentCents = 0;
		loanGregToJohn.createdAt = new Timestamp(System.currentTimeMillis());
		loanGregToJohn.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanGregToJohn);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(john.id);
		assertEquals(0, fromDb);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(paul.id);
		assertEquals(1, fromDb);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(greg.id);
		assertEquals(1, fromDb);

		Loan loanPaulToJohn2 = new Loan();
		loanPaulToJohn2.id = -1;
		loanPaulToJohn2.lenderId = paul.id;
		loanPaulToJohn2.borrowerId = john.id;
		loanPaulToJohn2.principalCents = 250 * 100; // $250
		loanPaulToJohn2.principalRepaymentCents = 0;
		loanPaulToJohn2.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohn2.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohn2);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(john.id);
		assertEquals(0, fromDb);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(paul.id);
		assertEquals(2, fromDb);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(greg.id);
		assertEquals(1, fromDb);
		
		Loan loanPaulToJohnDeleted = new Loan();
		loanPaulToJohnDeleted.id = -1;
		loanPaulToJohnDeleted.lenderId = paul.id;
		loanPaulToJohnDeleted.borrowerId = john.id;
		loanPaulToJohnDeleted.principalCents = 123 * 100; // $123
		loanPaulToJohnDeleted.principalRepaymentCents = 0;
		loanPaulToJohnDeleted.deleted = true;
		loanPaulToJohnDeleted.deletedReason = "wrong amount";
		loanPaulToJohnDeleted.deletedAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohnDeleted.createdAt = new Timestamp(System.currentTimeMillis());
		loanPaulToJohnDeleted.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loanPaulToJohnDeleted);
		
		fromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(paul.id);
		assertEquals(2, fromDb);
	}
	
	
}
