package me.timothy.bots;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.database.AdminUpdateMapping;
import me.timothy.bots.database.BannedUserMapping;
import me.timothy.bots.database.CreationInfoMapping;
import me.timothy.bots.database.FullnameMapping;
import me.timothy.bots.database.LCCMapping;
import me.timothy.bots.database.LoanMapping;
import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.database.RecentPostMapping;
import me.timothy.bots.database.RecheckMapping;
import me.timothy.bots.database.RepaymentMapping;
import me.timothy.bots.database.ResetPasswordRequestMapping;
import me.timothy.bots.database.ResponseHistoryMapping;
import me.timothy.bots.database.ResponseMapping;
import me.timothy.bots.database.SchemaValidator;
import me.timothy.bots.database.ShareCodeMapping;
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
import me.timothy.bots.database.mysql.MysqlRepaymentMapping;
import me.timothy.bots.database.mysql.MysqlResetPasswordRequestMapping;
import me.timothy.bots.database.mysql.MysqlResponseHistoryMapping;
import me.timothy.bots.database.mysql.MysqlResponseMapping;
import me.timothy.bots.database.mysql.MysqlShareCodeMapping;
import me.timothy.bots.database.mysql.MysqlUserMapping;
import me.timothy.bots.database.mysql.MysqlUsernameMapping;
import me.timothy.bots.database.mysql.MysqlWarningMapping;
import me.timothy.bots.models.Fullname;

/**
 * An implementation of a mapping database for the MySQL mappings.
 * 
 * @author Timothy
 */
public class LoansDatabase extends Database implements MappingDatabase {
	private Logger logger;
	private Connection connection;
	
	/*
	 * This is quite.. wordy. But schema validators are different from mappings,
	 * particularly when networking is involved (which is a planned addition). In that 
	 * cause, its quite likely I'll want to have seperate classes for it, since they will
	 * all have the same schema validator (verifies the protocol version).
	 * 
	 * Arrays aren't used because this is very business-logic-like; I may need to handle
	 * certain mappings "specially" at some point in the future, and a map / list would
	 * complicate that. Also, the getXMapping() pattern actually looks pretty nice
	 * when it's being used, and an array wouldn't look significantly better for that
	 * pattern.
	 */
	
	private AdminUpdateMapping adminUpdateMapping;
	private CreationInfoMapping creationInfoMapping;
	private FullnameMapping fullnameMapping;
	private LCCMapping lccMapping;
	private LoanMapping loanMapping;
	private RecheckMapping recheckMapping;
	private RepaymentMapping repaymentMapping;
	private ResetPasswordRequestMapping resetPasswordRequestMapping;
	private ResponseHistoryMapping responseHistoryMapping;
	private ResponseMapping responseMapping;
	private ShareCodeMapping shareCodeMapping;
	private UserMapping userMapping;
	private UsernameMapping usernameMapping;
	private WarningMapping warningMapping;
	private RecentPostMapping recentPostsMapping;
	private BannedUserMapping bannedUsersMapping;
	
	private SchemaValidator adminUpdateValidator;
	private SchemaValidator creationInfoValidator;
	private SchemaValidator fullnameValidator;
	private SchemaValidator lccValidator;
	private SchemaValidator loanValidator;
	private SchemaValidator recheckValidator;
	private SchemaValidator repaymentValidator;
	private SchemaValidator resetPasswordRequestValidator;
	private SchemaValidator responseHistoryValidator;
	private SchemaValidator responseValidator;
	private SchemaValidator shareCodeValidator;
	private SchemaValidator userValidator;
	private SchemaValidator usernameValidator;
	private SchemaValidator warningValidator;
	private SchemaValidator recentPostsValidator;
	private SchemaValidator bannedUsersValidator;
	
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
	public void connect(String username, String password, String url)
			throws SQLException {
		if (connection != null) {
			disconnect();
		}
		
		connection = DriverManager.getConnection(url, username, password);
		
		fullnameMapping = new MysqlFullnameMapping(this, connection);
		adminUpdateMapping = new MysqlAdminUpdateMapping(this, connection);
		creationInfoMapping = new MysqlCreationInfoMapping(this, connection);
		lccMapping = new MysqlLCCMapping(this, connection);
		loanMapping = new MysqlLoanMapping(this, connection);
		recheckMapping = new MysqlRecheckMapping(this, connection);
		repaymentMapping = new MysqlRepaymentMapping(this, connection);
		resetPasswordRequestMapping = new MysqlResetPasswordRequestMapping(this, connection);
		responseHistoryMapping = new MysqlResponseHistoryMapping(this, connection);
		responseMapping = new MysqlResponseMapping(this, connection);
		shareCodeMapping = new MysqlShareCodeMapping(this, connection);
		userMapping = new MysqlUserMapping(this, connection);
		usernameMapping = new MysqlUsernameMapping(this, connection);
		warningMapping = new MysqlWarningMapping(this, connection);
		recentPostsMapping = new MysqlRecentPostMapping(this, connection);
		bannedUsersMapping = new MysqlBannedUserMapping(this, connection);
		
		fullnameValidator = (SchemaValidator) fullnameMapping;
		adminUpdateValidator = (SchemaValidator) adminUpdateMapping;
		creationInfoValidator = (SchemaValidator) creationInfoMapping;
		lccValidator = (SchemaValidator) lccMapping;
		loanValidator = (SchemaValidator) loanMapping;
		recheckValidator = (SchemaValidator) recheckMapping;
		repaymentValidator = (SchemaValidator) repaymentMapping;
		resetPasswordRequestValidator = (SchemaValidator) resetPasswordRequestMapping;
		responseHistoryValidator = (SchemaValidator) responseHistoryMapping;
		responseValidator = (SchemaValidator) responseMapping;
		shareCodeValidator = (SchemaValidator) shareCodeMapping;
		userValidator = (SchemaValidator) userMapping;
		usernameValidator = (SchemaValidator) usernameMapping;
		warningValidator = (SchemaValidator) warningMapping;
		recentPostsValidator = (SchemaValidator) recentPostsMapping;
		bannedUsersValidator = (SchemaValidator) bannedUsersMapping;
	}
	
	/**
	 * Purges everything from everything. Scary stuff.
	 * 
	 * @see me.timothy.bots.database.SchemaValidator#purgeSchema()
	 */
	public void purgeAll() {
		// REVERSE ORDER of validateTableState
		bannedUsersValidator.purgeSchema();
		recentPostsValidator.purgeSchema();
		warningValidator.purgeSchema();
		shareCodeValidator.purgeSchema();
		responseHistoryValidator.purgeSchema();
		responseValidator.purgeSchema();
		resetPasswordRequestValidator.purgeSchema();
		repaymentValidator.purgeSchema();
		recheckValidator.purgeSchema();
		lccValidator.purgeSchema();
		creationInfoValidator.purgeSchema();
		adminUpdateValidator.purgeSchema();
		loanValidator.purgeSchema();
		usernameValidator.purgeSchema();
		userValidator.purgeSchema();
		fullnameValidator.purgeSchema();
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
		fullnameValidator.validateSchema();
		userValidator.validateSchema();
		usernameValidator.validateSchema();
		loanValidator.validateSchema();
		adminUpdateValidator.validateSchema();
		creationInfoValidator.validateSchema();
		lccValidator.validateSchema();
		recheckValidator.validateSchema();
		repaymentValidator.validateSchema();
		resetPasswordRequestValidator.validateSchema();
		responseValidator.validateSchema();
		responseHistoryValidator.validateSchema();
		shareCodeValidator.validateSchema();
		warningValidator.validateSchema();
		recentPostsValidator.validateSchema();
		bannedUsersValidator.validateSchema();
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
		
		adminUpdateMapping = null;
		creationInfoMapping = null;
		fullnameMapping = null;
		lccMapping = null;
		loanMapping = null;
		recheckMapping = null;
		repaymentMapping = null;
		resetPasswordRequestMapping = null;
		responseHistoryMapping = null;
		responseMapping = null;
		shareCodeMapping = null;
		userMapping = null;
		usernameMapping = null;
		warningMapping = null;
		recentPostsMapping = null;
		bannedUsersMapping = null;
		
		adminUpdateValidator = null;
		creationInfoValidator = null;
		fullnameValidator = null;
		lccValidator = null;
		loanValidator = null;
		recheckValidator = null;
		repaymentValidator = null;
		resetPasswordRequestValidator = null;
		responseHistoryValidator = null;
		responseValidator = null;
		shareCodeValidator = null;
		userValidator = null;
		usernameValidator = null;
		warningValidator = null;
		recentPostsValidator = null;
		bannedUsersValidator = null;
	}
	
	public AdminUpdateMapping getAdminUpdateMapping() {
		return adminUpdateMapping;
	}
	public CreationInfoMapping getCreationInfoMapping() {
		return creationInfoMapping;
	}
	public FullnameMapping getFullnameMapping() {
		return fullnameMapping;
	}
	public LCCMapping getLccMapping() {
		return lccMapping;
	}
	public LoanMapping getLoanMapping() {
		return loanMapping;
	}
	public RecheckMapping getRecheckMapping() {
		return recheckMapping;
	}
	public RepaymentMapping getRepaymentMapping() {
		return repaymentMapping;
	}
	public ResetPasswordRequestMapping getResetPasswordRequestMapping() {
		return resetPasswordRequestMapping;
	}
	public ResponseHistoryMapping getResponseHistoryMapping() {
		return responseHistoryMapping;
	}
	public ResponseMapping getResponseMapping() {
		return responseMapping;
	}
	public ShareCodeMapping getShareCodeMapping() {
		return shareCodeMapping;
	}
	public UserMapping getUserMapping() {
		return userMapping;
	}
	public UsernameMapping getUsernameMapping() {
		return usernameMapping;
	}
	public WarningMapping getWarningMapping() {
		return warningMapping;
	}
	public RecentPostMapping getRecentPostMapping() {
		return recentPostsMapping;
	}
	public BannedUserMapping getBannedUserMapping() {
		return bannedUsersMapping;
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
		fullnameMapping.save(new Fullname(-1, id));
	}

	/**
	 * Scans the database for ids matching the specified id
	 * 
	 * @param id the id to scan for
	 * @return if the database has that id
	 */
	@Override
	public boolean containsFullname(String id) {
		return fullnameMapping.contains(id);
	}
}
