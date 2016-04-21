package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;

/**
 * Describes a test focused on a LoanMapping in a MappingDatabase. The database
 * must be <i>completely</i> empty after each setUp. The database <i>will</i> be
 * modified after each run - do not run against real databases.
 * 
 * @author Timothy
 */
public class LoanMappingTest {
	/**
	 * The {@link MappingDatabase MappingDatabase} that contains the
	 * {@link me.timothy.bots.database.LoanMapping LoanMapping} to test.
	 */
	protected MappingDatabase database;

	/**
	 * Tests to see that the test is setup correctly by verifying that the
	 * {@link #database database} is not null.
	 */
	@Test
	public void testTest() {
		assertNotNull(database);
	}

	/**
	 * Tests that simple {@link Loan} saving can be done without error by
	 * verifying that, after
	 * {@link me.timothy.bots.database.ObjectMapping#save(Object) saving}:
	 * <ul>
	 * <li>the {@link Loan}.{@link Loan#id id} is strictly positive</li>
	 * <li>when all {@link Loan Loans} are
	 * {@link me.timothy.bots.database.ObjectMapping#fetchAll() fetched}, only
	 * the {@link Loan Loans} that have been
	 * {@link me.timothy.bots.database.ObjectMapping#save(Object) saved} are
	 * returned, using an {@link Object#equals(Object) equality} check.</li>
	 * </ul>
	 */
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
		assertListContents(fromDb, loan);
	}

	/**
	 * Tests
	 * {@link me.timothy.bots.database.LoanMapping#fetchLenderIdsWithNewLoanSince(Timestamp)
	 * LoanMapping#fetchLenderIdsWithNewLoanSince(Timestamp)}.
	 * <ul>
	 * <li>Only returns {@link User#id user ids} that have a {@link Loan}
	 * {@link Loan#createdAt created at} as {@link Loan#lenderId lender}
	 * <i>after</i> the {@link Timestamp} that is sent to it.</li>
	 * <li>Does not duplicate {@link User#id user ids}.</li>
	 * </ul>
	 */
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
		assertListContents(fromDb, paul.id);

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
		assertListContents(fromDb, paul.id, greg.id);

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
		assertListContents(fromDb, paul.id, greg.id); // don't duplicate user ids
	}

	/**
	 * Tests
	 * {@link me.timothy.bots.database.LoanMapping#fetchWithBorrowerAndOrLender(int, int, boolean)
	 * LoanMapping#fetchWithBorrowerAndOrLender(int, int, boolean)} (from here
	 * on, just the test function)
	 * 
	 * <ul>
	 * <li>When the {@code strict} tag is set to {@code false}, the test
	 * function returns {@link Loan loans} in which the lender <b>or</b> the
	 * borrower matches the respective {@link Loan#lenderId lenderId} or
	 * {@link Loan#borrowerId borrowerId}.</li>
	 * <li>When the {@code strict} tag is set to {@code true}, the test function
	 * returns {@link Loan loans} that match <b>both</b> the
	 * {@link Loan#lenderId} and {@link Loan#borrowerId}.</li>
	 * <li>Regardless of the {@code strict} tag, the test function
	 * <i>ignores</i> {@link Loan#deleted deleted} {@link Loan loans}</li>
	 * </ul>
	 */
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
		assertListContents(fromDb, loanPaulToJohn);

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, john.id, false);
		assertListContents(fromDb, loanPaulToJohn);

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(paul.id, paul.id, true);
		assertEquals(0, fromDb.size());

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, john.id, true);
		assertEquals(0, fromDb.size());

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, true);
		assertListContents(fromDb, loanPaulToJohn);

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, false);
		assertListContents(fromDb, loanPaulToJohn);

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
		assertListContents(fromDb, loanPaulToJohn, loanGregToJohn);

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, greg.id, false);
		assertListContents(fromDb, loanPaulToJohn, loanGregToJohn);

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, greg.id, true);
		assertListContents(fromDb, loanGregToJohn);
		
		loanGregToJohn.deleted = true;
		loanGregToJohn.deletedAt = new Timestamp(System.currentTimeMillis());
		loanGregToJohn.deletedReason = "testing";
		database.getLoanMapping().save(loanGregToJohn);
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, john.id, false);
		assertListContents(fromDb, loanPaulToJohn);
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, greg.id, false);
		assertListContents(fromDb, loanPaulToJohn);
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, greg.id, true);
		assertEquals(0, fromDb.size());
		
		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, true);
		assertListContents(fromDb, loanPaulToJohn);
	}

	/**
	 * Tests 
	 * {@link me.timothy.bots.database.LoanMapping#fetchNumberOfLoansWithUserAsLender(int) 
	 * LoanMapping.fetchNumberOfLoansWithUserAsLender(int)}
	 * 
	 * <ul>
	 * <li>Should only return the count of {@link Loan loans} as {@link Loan#lenderId lender}</li>
	 * <li>Should <i>not</i> count {@link Loan#deleted deleted} {@link Loan loans}.</li>
	 * </ul>
	 */
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

	/**
	 * This contains further tests to verify the
	 * {@link me.timothy.bots.database.LoanMapping LoanMapping} handles 
	 * {@link Loan#deleted deleted} {@link Loan loans} appropriately.
	 * 
	 * <ul>
	 * <li>{@link me.timothy.bots.database.ObjectMapping#fetchAll() fetchAll()}
	 * <b>should</b> return {@link Loan#deleted deleted} {@link Loan loans}</li>
	 * <li>{@link me.timothy.bots.database.LoanMapping#fetchWithBorrowerAndOrLender(int, int, boolean)
	 * fetchWithBorrowerAndOrLender(int, int, boolean)} <b>should not</b> return 
	 * {@link Loan#deleted deleted} {@link Loan loans}</li>
	 * <li>{@link me.timothy.bots.database.LoanMapping#fetchNumberOfLoansWithUserAsLender(int)
	 * fetchNumberOfLoansWithUserAsLender(int)} <b>should not</b> return {@link Loan#deleted deleted}
	 * {@link Loan loans}</li>
	 * <li>{@link me.timothy.bots.database.LoanMapping#fetchLenderIdsWithNewLoanSince(Timestamp)
	 * fetchLenderIdsWithNewLoanSince(Timestamp)} <b>should not</b> return {@link Loan#deleted deleted}
	 * {@link Loan loans}</li>
	 * </ul>
	 */
	@Test
	public void testFetchDeleted() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Timestamp past = new Timestamp(System.currentTimeMillis() - 60000);
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");

		Loan loanPaulToJohn1 = new Loan();
		loanPaulToJohn1.id = -1;
		loanPaulToJohn1.lenderId = paul.id;
		loanPaulToJohn1.borrowerId = john.id;
		loanPaulToJohn1.principalCents = 100 * 100; // $100
		loanPaulToJohn1.principalRepaymentCents = 0;
		loanPaulToJohn1.createdAt = now;
		loanPaulToJohn1.updatedAt = now;
		database.getLoanMapping().save(loanPaulToJohn1);

		List<Loan> fromDb = database.getLoanMapping().fetchAll();
		assertListContents(fromDb, loanPaulToJohn1);

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, true);
		assertListContents(fromDb, loanPaulToJohn1);

		loanPaulToJohn1.deleted = true;
		loanPaulToJohn1.deletedAt = now;
		loanPaulToJohn1.deletedReason = "invalid";
		database.getLoanMapping().save(loanPaulToJohn1);

		fromDb = database.getLoanMapping().fetchAll();
		assertListContents(fromDb, loanPaulToJohn1);

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, true);
		assertEquals(0, fromDb.size());

		fromDb = database.getLoanMapping().fetchWithBorrowerAndOrLender(john.id, paul.id, false);
		assertEquals(0, fromDb.size());
		
		int iFromDb = database.getLoanMapping().fetchNumberOfLoansWithUserAsLender(paul.id);
		assertEquals(0, iFromDb);
		
		List<Integer> liFromDb = database.getLoanMapping().fetchLenderIdsWithNewLoanSince(past);
		assertEquals(0, liFromDb.size());
	}
}
