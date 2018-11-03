package me.timothy.tests.database.mysql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(
		{
			MysqlAdminUpdateMappingTest.class,
			MysqlCreationInfoMappingTest.class,
			MysqlFullnameMappingTest.class,
			MysqlLCCMappingTest.class, 
			MysqlLoanMappingTest.class,
			MysqlUserMappingTest.class,
			MysqlUsernameMappingTest.class,
			MysqlRecheckMappingTest.class,
			MysqlRepaymentMappingTest.class,
			MysqlResetPasswordRequestMappingTest.class, 
			MysqlResponseMappingTest.class, 
			MysqlResponseHistoryMappingTest.class,
			MysqlShareCodeMappingTest.class,
			MysqlWarningMappingTest.class,
			MysqlRecentPostMappingTest.class,
			MysqlBannedUserMappingTest.class,
			MysqlRedFlagReportMappingTest.class,
			MysqlRedFlagMappingTest.class,
			MysqlRedFlagQueueSpotMappingTest.class,
			MysqlRedFlagUserHistoryCommentMappingTest.class,
			MysqlRedFlagUserHistoryLinkMappingTest.class,
			MysqlRedFlagUserHistorySortMappingTest.class
		})
public class MysqlDatabaseTests {

}
