package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.AdminUpdate;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;

/**
 * Describes tests focused on testing an AdminUpdateMapping inside
 * a MappingDatabase. The database should be <i>completely</i> empty
 * before each test. The database <i>will</i> be modified. This should
 * <i>not</i> be run against a production database.
 * 
 * @author Timothy
 */
public class AdminUpdateMappingTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}

	@Test
	public void testSave() {
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		User john = database.getUserMapping().fetchOrCreateByName("john");
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		paul.auth = 10;
		database.getUserMapping().save(paul);
		
		Loan loan = new Loan();
		loan.id = -1;
		loan.lenderId = paul.id;
		loan.borrowerId = john.id;
		loan.principalCents = 100 * 100; // $100
		loan.principalRepaymentCents = 0;
		loan.createdAt = new Timestamp(System.currentTimeMillis());
		loan.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getLoanMapping().save(loan);
		
		AdminUpdate adminUpdate = new AdminUpdate();
		adminUpdate.id = -1;
		adminUpdate.userId = paul.id;
		adminUpdate.loanId = loan.id;
		adminUpdate.oldLenderId = loan.lenderId;
		adminUpdate.oldBorrowerId = loan.borrowerId;
		adminUpdate.oldPrincipalCents = loan.principalCents;
		adminUpdate.oldPrincipalRepaymentCents = loan.principalRepaymentCents;
		adminUpdate.newLenderId = greg.id;
		adminUpdate.newBorrowerId = john.id;
		adminUpdate.newPrincipalCents = loan.principalCents;
		adminUpdate.newPrincipalRepaymentCents = loan.principalRepaymentCents;
		adminUpdate.reason = "wrong lender (paul tried to show greg command)";
		adminUpdate.createdAt = new Timestamp(System.currentTimeMillis());
		adminUpdate.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getAdminUpdateMapping().save(adminUpdate);
		
		loan.lenderId = greg.id;
		database.getLoanMapping().save(loan);
		
		
		assertTrue(adminUpdate.id > 0);
		
		List<AdminUpdate> fromDb = database.getAdminUpdateMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + adminUpdate, fromDb.contains(adminUpdate));
	}
}
