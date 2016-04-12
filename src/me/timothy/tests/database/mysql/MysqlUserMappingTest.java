package me.timothy.tests.database.mysql;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;

import me.timothy.bots.LoansDatabase;
import me.timothy.tests.database.UserMappingTest;

public class MysqlUserMappingTest extends UserMappingTest {

	@Before
	public void setUp() {
		Properties testDBProperties = MysqlTestUtils.fetchTestDatabaseProperties();
		LoansDatabase testDb = MysqlTestUtils.getDatabase(testDBProperties);
		MysqlTestUtils.clearDatabase(testDb);
		
		super.database = testDb;
	}

	@After
	public void tearDown() {
		((LoansDatabase) database).disconnect();
	}
}
