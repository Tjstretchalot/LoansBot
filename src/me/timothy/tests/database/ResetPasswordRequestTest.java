package me.timothy.tests.database;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;

import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.models.ResetPasswordRequest;
import me.timothy.bots.models.User;

/**
 * Describes a test focused on a ResetPasswordRequest inside
 * a MappingDatabase. The database must be cleared prior to each
 * test. This test modifies the database. Do not run against
 * a production database.
 * 
 * @author Timothy
 */
public class ResetPasswordRequestTest {
	protected MappingDatabase database;
	
	@Test
	public void testTest() {
		assertNotNull(database);
	}
	
	@Test
	public void testSave() {
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		ResetPasswordRequest rprGreg = new ResetPasswordRequest();
		rprGreg.id = -1;
		rprGreg.userId = greg.id;
		rprGreg.resetCode = "asdf";
		rprGreg.resetCodeSent = false;
		rprGreg.resetCodeUsed = false;
		rprGreg.createdAt = new Timestamp(System.currentTimeMillis());
		rprGreg.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getResetPasswordRequestMapping().save(rprGreg);
		assertTrue(rprGreg.id > 0);
		
		List<ResetPasswordRequest> fromDb = database.getResetPasswordRequestMapping().fetchAll();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + rprGreg, fromDb.contains(rprGreg));
	}
	
	@Test
	public void testFetchUnsent() {
		User greg = database.getUserMapping().fetchOrCreateByName("greg");
		
		ResetPasswordRequest rprGreg = new ResetPasswordRequest();
		rprGreg.id = -1;
		rprGreg.userId = greg.id;
		rprGreg.resetCode = "asdf";
		rprGreg.resetCodeSent = false;
		rprGreg.resetCodeUsed = false;
		rprGreg.createdAt = new Timestamp(System.currentTimeMillis());
		rprGreg.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getResetPasswordRequestMapping().save(rprGreg);
		
		List<ResetPasswordRequest> fromDb = database.getResetPasswordRequestMapping().fetchUnsent();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + rprGreg, fromDb.contains(rprGreg));
		
		User paul = database.getUserMapping().fetchOrCreateByName("paul");
		
		ResetPasswordRequest rprPaul = new ResetPasswordRequest();
		rprPaul.id = -1;
		rprPaul.userId = paul.id;
		rprPaul.resetCode = "kasjdf";
		rprPaul.resetCodeSent = false;
		rprPaul.resetCodeUsed = false;
		rprPaul.createdAt = new Timestamp(System.currentTimeMillis());
		rprPaul.updatedAt = new Timestamp(System.currentTimeMillis());
		database.getResetPasswordRequestMapping().save(rprPaul);
		
		fromDb = database.getResetPasswordRequestMapping().fetchUnsent();
		assertEquals(2, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + rprPaul, fromDb.contains(rprPaul));
		assertTrue("expected " + fromDb + " to contain " + rprGreg, fromDb.contains(rprGreg));
		
		rprGreg.resetCodeSent = true;
		database.getResetPasswordRequestMapping().save(rprGreg);
		
		fromDb = database.getResetPasswordRequestMapping().fetchUnsent();
		assertEquals(1, fromDb.size());
		assertTrue("expected " + fromDb + " to contain " + rprPaul, fromDb.contains(rprPaul));
	}
}
