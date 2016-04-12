package me.timothy.tests.database.mysql;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import me.timothy.bots.LoansDatabase;
import me.timothy.tests.database.RecheckMappingTest;

public class MysqlRecheckMappingTest extends RecheckMappingTest {
	@Before
	public void setUp() {
		Properties testDBProperties = MysqlTestUtils.fetchTestDatabaseProperties();
		LoansDatabase testDb = MysqlTestUtils.getDatabase(testDBProperties);
		MysqlTestUtils.clearDatabase(testDb);
		
		super.database = testDb;
	}
	
	@After
	public void tearDown() {
		((LoansDatabase) super.database).disconnect();
	}

}
