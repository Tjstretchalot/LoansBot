package me.timothy.tests.database;

import static org.junit.Assert.*;
import static me.timothy.tests.database.mysql.MysqlTestUtils.assertListContents;

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
	/**
	 * The mapping database containing the 
	 * {@link me.timothy.bots.database.AdminUpdateMapping AdminUpdateMapping} 
	 * to be tested. Must be initialized by a child class.
	 */
	protected MappingDatabase database;
	
	/**
	 * <p>Verifies that the test has been prepared correctly
	 * by the subclass by asserting that the {@link #database database} is 
	 * not null.</p>
	 */
	@Test
	public void testTest() {
		assertNotNull(database);
	}

	/**
	 * <p>Tests the ability to save {@link me.timothy.bots.models.AdminUpdate AdminUpdates}</p>
	 * 
	 * <p>This is a basic test to make sure that doing a standard
	 * {@link me.timothy.bots.database.ObjectMapping#save(Object) save} on 
	 * an {@link me.timothy.bots.models.AdminUpdate AdminUpdate} does not result 
	 * in any errors, and sets the {@link me.timothy.bots.models.AdminUpdate#id id} of the 
	 * {@link me.timothy.bots.models.AdminUpdate AdminUpdate}
	 * to a positive integer, and the very same (using 
	 * {@link me.timothy.bots.models.AdminUpdate#equals(Object) equals}) 
	 * {@link me.timothy.bots.models.AdminUpdate AdminUpdate} is returned using 
	 * {@link me.timothy.bots.database.ObjectMapping#fetchAll() fetchAll}</p>
	 * 
	 * <p>This has the side-effect of checking 
	 * {@link AdminUpdate#equals(Object) AdminUpdates equals function}
	 * is implemented.</p>
	 */
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
		assertListContents(fromDb, adminUpdate);
	}
}
