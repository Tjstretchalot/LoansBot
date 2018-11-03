package me.timothy.bots;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.database.AdminUpdateMapping;
import me.timothy.bots.database.BannedUserMapping;
import me.timothy.bots.database.CreationInfoMapping;
import me.timothy.bots.database.FullnameMapping;
import me.timothy.bots.database.LCCMapping;
import me.timothy.bots.database.LoanMapping;
import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.database.ObjectMapping;
import me.timothy.bots.database.RecentPostMapping;
import me.timothy.bots.database.RecheckMapping;
import me.timothy.bots.database.RedFlagForSubredditMapping;
import me.timothy.bots.database.RedFlagMapping;
import me.timothy.bots.database.RedFlagQueueSpotMapping;
import me.timothy.bots.database.RedFlagReportMapping;
import me.timothy.bots.database.RedFlagUserHistoryCommentMapping;
import me.timothy.bots.database.RedFlagUserHistoryLinkMapping;
import me.timothy.bots.database.RedFlagUserHistorySortMapping;
import me.timothy.bots.database.RepaymentMapping;
import me.timothy.bots.database.ResetPasswordRequestMapping;
import me.timothy.bots.database.ResponseHistoryMapping;
import me.timothy.bots.database.ResponseMapping;
import me.timothy.bots.database.SavedQueryMapping;
import me.timothy.bots.database.SavedQueryParamMapping;
import me.timothy.bots.database.SavedQueryUserMapping;
import me.timothy.bots.database.SchemaValidator;
import me.timothy.bots.database.ShareCodeMapping;
import me.timothy.bots.database.SiteSessionMapping;
import me.timothy.bots.database.UserMapping;
import me.timothy.bots.database.UsernameMapping;
import me.timothy.bots.database.WarningMapping;
import me.timothy.bots.database.mysql.MysqlAdminUpdateMapping;
import me.timothy.bots.database.mysql.MysqlBannedUserMapping;
import me.timothy.bots.database.mysql.MysqlCreationInfoMapping;
import me.timothy.bots.database.mysql.MysqlFullnameMapping;
import me.timothy.bots.database.mysql.MysqlLCCMapping;
import me.timothy.bots.database.mysql.MysqlLoanMapping;
import me.timothy.bots.database.mysql.MysqlRecentPostMapping;
import me.timothy.bots.database.mysql.MysqlRecheckMapping;
import me.timothy.bots.database.mysql.MysqlRedFlagForSubredditMapping;
import me.timothy.bots.database.mysql.MysqlRedFlagMapping;
import me.timothy.bots.database.mysql.MysqlRedFlagQueueSpotMapping;
import me.timothy.bots.database.mysql.MysqlRedFlagReportMapping;
import me.timothy.bots.database.mysql.MysqlRedFlagUserHistoryCommentMapping;
import me.timothy.bots.database.mysql.MysqlRedFlagUserHistoryLinkMapping;
import me.timothy.bots.database.mysql.MysqlRedFlagUserHistorySortMapping;
import me.timothy.bots.database.mysql.MysqlRepaymentMapping;
import me.timothy.bots.database.mysql.MysqlResetPasswordRequestMapping;
import me.timothy.bots.database.mysql.MysqlResponseHistoryMapping;
import me.timothy.bots.database.mysql.MysqlResponseMapping;
import me.timothy.bots.database.mysql.MysqlSavedQueryMapping;
import me.timothy.bots.database.mysql.MysqlSavedQueryParamMapping;
import me.timothy.bots.database.mysql.MysqlSavedQueryUserMapping;
import me.timothy.bots.database.mysql.MysqlShareCodeMapping;
import me.timothy.bots.database.mysql.MysqlSiteSessionMapping;
import me.timothy.bots.database.mysql.MysqlUserMapping;
import me.timothy.bots.database.mysql.MysqlUsernameMapping;
import me.timothy.bots.database.mysql.MysqlWarningMapping;
import me.timothy.bots.models.AdminUpdate;
import me.timothy.bots.models.BannedUser;
import me.timothy.bots.models.CreationInfo;
import me.timothy.bots.models.Fullname;
import me.timothy.bots.models.LendersCampContributor;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.RecentPost;
import me.timothy.bots.models.Recheck;
import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagForSubreddit;
import me.timothy.bots.models.RedFlagQueueSpot;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.RedFlagUserHistorySort;
import me.timothy.bots.models.Repayment;
import me.timothy.bots.models.ResetPasswordRequest;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.ResponseHistory;
import me.timothy.bots.models.SavedQuery;
import me.timothy.bots.models.SavedQueryParam;
import me.timothy.bots.models.SavedQueryUser;
import me.timothy.bots.models.ShareCode;
import me.timothy.bots.models.SiteSession;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.models.Warning;

/**
 * An implementation of a mapping database for the MySQL mappings.
 * 
 * @author Timothy
 */
public class LoansDatabase extends Database implements MappingDatabase {
	private Logger logger;
	private Connection connection;
	
	private List<ObjectMapping<?>> mappings;
	private Map<Class<?>, ObjectMapping<?>> mappingsDict;
	
	public LoansDatabase() {
		logger = LogManager.getLogger();
	}
	
	/**
	 * Connects to the specified database. If there is an active connection
	 * already, the active connection is explicity closed.
	 * 
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param url
	 *            the url
	 * @throws SQLException
	 *             if a sql-related exception occurs
	 */
	public void connect(String username, String password, String url, boolean useSSL)
			throws SQLException {
		if (connection != null) {
			disconnect();
		}
		
		String trueURL = url + "?useSSL=" + useSSL;
		connection = DriverManager.getConnection(trueURL, username, password);
		
		mappings = new ArrayList<>();
		mappingsDict = new HashMap<>();
		addMapping(Fullname.class, new MysqlFullnameMapping(this, connection));
		addMapping(User.class, new MysqlUserMapping(this, connection));
		addMapping(Username.class, new MysqlUsernameMapping(this, connection));
		addMapping(Loan.class, new MysqlLoanMapping(this, connection));
		addMapping(Response.class, new MysqlResponseMapping(this, connection));
		addMapping(AdminUpdate.class, new MysqlAdminUpdateMapping(this, connection));
		addMapping(CreationInfo.class, new MysqlCreationInfoMapping(this, connection));
		addMapping(LendersCampContributor.class, new MysqlLCCMapping(this, connection));
		addMapping(Recheck.class, new MysqlRecheckMapping(this, connection));
		addMapping(Repayment.class, new MysqlRepaymentMapping(this, connection));
		addMapping(ResetPasswordRequest.class, new MysqlResetPasswordRequestMapping(this, connection));
		addMapping(ResponseHistory.class, new MysqlResponseHistoryMapping(this, connection));
		addMapping(ShareCode.class, new MysqlShareCodeMapping(this, connection));
		addMapping(Warning.class, new MysqlWarningMapping(this, connection));
		addMapping(RecentPost.class, new MysqlRecentPostMapping(this, connection));
		addMapping(BannedUser.class, new MysqlBannedUserMapping(this, connection));
		addMapping(SiteSession.class, new MysqlSiteSessionMapping(this, connection));
		addMapping(SavedQuery.class, new MysqlSavedQueryMapping(this, connection));
		addMapping(SavedQueryParam.class, new MysqlSavedQueryParamMapping(this, connection));
		addMapping(SavedQueryUser.class, new MysqlSavedQueryUserMapping(this, connection));
		addMapping(RedFlagReport.class, new MysqlRedFlagReportMapping(this, connection));
		addMapping(RedFlag.class, new MysqlRedFlagMapping(this, connection));
		addMapping(RedFlagQueueSpot.class, new MysqlRedFlagQueueSpotMapping(this, connection));
		addMapping(RedFlagForSubreddit.class, new MysqlRedFlagForSubredditMapping(this, connection));
		addMapping(RedFlagUserHistoryComment.class, new MysqlRedFlagUserHistoryCommentMapping(this, connection));
		addMapping(RedFlagUserHistoryLink.class, new MysqlRedFlagUserHistoryLinkMapping(this, connection));
		addMapping(RedFlagUserHistorySort.class, new MysqlRedFlagUserHistorySortMapping(this, connection));
	}
	
	private <A> void addMapping(Class<A> cl, ObjectMapping<A> mapping) {
		mappings.add(mapping);
		mappingsDict.put(cl, mapping);
	}
	
	/**
	 * Purges everything from everything. Scary stuff.
	 * 
	 * @see me.timothy.bots.database.SchemaValidator#purgeSchema()
	 */
	public void purgeAll() {
		for(int i = mappings.size() - 1; i >= 0; i--) {
			((SchemaValidator)mappings.get(i)).purgeSchema();
		}
	}
	
	/**
	 * <p>Validates the tables in the database match what are expected. If the tables
	 * cannot be found, they are created. Throws an error if the tables already exist
	 * but are not in the expected state.</p>
	 * 
	 * @throws IllegalStateException if the tables are in the wrong state
	 * @see me.timothy.bots.database.SchemaValidator#validateSchema()
	 */
	public void validateTableState() {
		for(int i = 0; i < mappings.size(); i++) {
			((SchemaValidator)mappings.get(i)).validateSchema();
		}
	}
	
	/**
	 * Ensures the database is disconnected and will not return invalid
	 * mappings (instead they will return null until the next connect)
	 */
	public void disconnect() {
		try {
			connection.close();
		} catch (SQLException e) {
			logger.throwing(e);
		}
		
		mappings = null;
		mappingsDict = null;
	}
	
	public AdminUpdateMapping getAdminUpdateMapping() {
		return (AdminUpdateMapping) mappingsDict.get(AdminUpdate.class);
	}
	public CreationInfoMapping getCreationInfoMapping() {
		return (CreationInfoMapping) mappingsDict.get(CreationInfo.class);
	}
	public FullnameMapping getFullnameMapping() {
		return (FullnameMapping) mappingsDict.get(Fullname.class);
	}
	public LCCMapping getLccMapping() {
		return (LCCMapping) mappingsDict.get(LendersCampContributor.class);
	}
	public LoanMapping getLoanMapping() {
		return (LoanMapping) mappingsDict.get(Loan.class);
	}
	public RecheckMapping getRecheckMapping() {
		return (RecheckMapping) mappingsDict.get(Recheck.class);
	}
	public RepaymentMapping getRepaymentMapping() {
		return (RepaymentMapping) mappingsDict.get(Repayment.class);
	}
	public ResetPasswordRequestMapping getResetPasswordRequestMapping() {
		return (ResetPasswordRequestMapping) mappingsDict.get(ResetPasswordRequest.class);
	}
	public ResponseHistoryMapping getResponseHistoryMapping() {
		return (ResponseHistoryMapping) mappingsDict.get(ResponseHistory.class);
	}
	public ResponseMapping getResponseMapping() {
		return (ResponseMapping) mappingsDict.get(Response.class);
	}
	public ShareCodeMapping getShareCodeMapping() {
		return (ShareCodeMapping) mappingsDict.get(ShareCode.class);
	}
	public UserMapping getUserMapping() {
		return (UserMapping) mappingsDict.get(User.class);
	}
	public UsernameMapping getUsernameMapping() {
		return (UsernameMapping) mappingsDict.get(Username.class);
	}
	public WarningMapping getWarningMapping() {
		return (WarningMapping) mappingsDict.get(Warning.class);
	}
	public RecentPostMapping getRecentPostMapping() {
		return (RecentPostMapping) mappingsDict.get(RecentPost.class);
	}
	public BannedUserMapping getBannedUserMapping() {
		return (BannedUserMapping) mappingsDict.get(BannedUser.class);
	}
	public SiteSessionMapping getSiteSessionMapping() {
		return (SiteSessionMapping) mappingsDict.get(SiteSession.class);
	}
	public SavedQueryMapping getSavedQueryMapping() {
		return (SavedQueryMapping) mappingsDict.get(SavedQuery.class);
	}
	public SavedQueryParamMapping getSavedQueryParamMapping() {
		return (SavedQueryParamMapping) mappingsDict.get(SavedQueryParam.class);
	}
	public SavedQueryUserMapping getSavedQueryUserMapping() {
		return (SavedQueryUserMapping) mappingsDict.get(SavedQueryUser.class);
	}
	public RedFlagReportMapping getRedFlagReportMapping() {
		return (RedFlagReportMapping) mappingsDict.get(RedFlagReport.class);
	}
	public RedFlagMapping getRedFlagMapping() {
		return (RedFlagMapping) mappingsDict.get(RedFlag.class);
	}
	public RedFlagQueueSpotMapping getRedFlagQueueSpotMapping() {
		return (RedFlagQueueSpotMapping) mappingsDict.get(RedFlagQueueSpot.class);
	}
	public RedFlagForSubredditMapping getRedFlagForSubredditMapping() {
		return (RedFlagForSubredditMapping) mappingsDict.get(RedFlagForSubreddit.class);
	}
	public RedFlagUserHistoryCommentMapping getRedFlagUserHistoryCommentMapping() {
		return (RedFlagUserHistoryCommentMapping) mappingsDict.get(RedFlagUserHistoryComment.class);
	}
	public RedFlagUserHistoryLinkMapping getRedFlagUserHistoryLinkMapping() {
		return (RedFlagUserHistoryLinkMapping) mappingsDict.get(RedFlagUserHistoryLink.class);
	}
	public RedFlagUserHistorySortMapping getRedFlagUserHistorySortMapping() {
		return (RedFlagUserHistorySortMapping) mappingsDict.get(RedFlagUserHistorySort.class);
	}
	/*
	 * The following don't match the "MapperDatabase" I have setup, because I'm subclassing
	 * from the generic Database from SummonableBot. I'm brainstorming ways to refactor this
	 * without breaking other people codes - shoot me a message at mtimothy984@gmail.com or 
	 * comment here if you think of one!
	 */
	
	/**
	 * Adds a fullname to the database
	 * @param id the fullname to add
	 */
	@Override
	public void addFullname(String id) {
		getFullnameMapping().save(new Fullname(-1, id));
	}

	/**
	 * Scans the database for ids matching the specified id
	 * 
	 * @param id the id to scan for
	 * @return if the database has that id
	 */
	@Override
	public boolean containsFullname(String id) {
		return getFullnameMapping().contains(id);
	}
}
