package me.timothy.bots;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.database.AdminUpdateMapping;
import me.timothy.bots.database.CreationInfoMapping;
import me.timothy.bots.database.FullnameMapping;
import me.timothy.bots.database.LCCMapping;
import me.timothy.bots.database.LoanMapping;
import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.database.RecheckMapping;
import me.timothy.bots.database.RepaymentMapping;
import me.timothy.bots.database.ResetPasswordRequestMapping;
import me.timothy.bots.database.ResponseHistoryMapping;
import me.timothy.bots.database.ResponseMapping;
import me.timothy.bots.database.ShareCodeMapping;
import me.timothy.bots.database.UserMapping;
import me.timothy.bots.database.UsernameMapping;
import me.timothy.bots.database.WarningMapping;
import me.timothy.bots.database.mysql.MysqlAdminUpdateMapping;
import me.timothy.bots.database.mysql.MysqlCreationInfoMapping;
import me.timothy.bots.database.mysql.MysqlFullnameMapping;
import me.timothy.bots.database.mysql.MysqlLCCMapping;
import me.timothy.bots.database.mysql.MysqlLoanMapping;
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
 * 
 * 
 * @author Timothy
 */
public class LoansDatabase extends Database implements MappingDatabase {
	private Logger logger;
	private Connection connection;
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
	}
	
	/**
	 * Purges everything from everything. Scary stuff.
	 * 
	 */
	public void purgeAll() {
		try {
			Statement statement = connection.createStatement();
			statement.execute("DELETE FROM fullnames;");
		}catch(SQLException sqlE) {
			logger.throwing(sqlE);
			throw new RuntimeException(sqlE);
		}
	}
	
	/**
	 * <p>Validates the tables in the database match what are expected. If the tables
	 * cannot be found, they are created. Throws an error if the tables already exist
	 * but are not in the expected state.</p>
	 * 
	 * <p>Exactly the same as calling each validateXTable</p>
	 * 
	 * @throws IllegalStateException if the tables are in the wrong state
	 */
	public void validateTableState() {
		validateFullnamesTable();
	}
	
	public void validateFullnamesTable() {
		try {
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet results = metadata.getTables(null, null, "fullnames", null);
			boolean exists = results.next();
			results.close();
			
			if(!exists) {
				createFullnamesTable();
			}else {
				results = metadata.getColumns(null, null, "fullnames", "%"); 
				
				List<String> errors = new ArrayList<>();
				List<String> toFind = new ArrayList<>();
				toFind.add("id");
				toFind.add("fullname");
				
				while(results.next()) {
					int type;
					String autoIncrement;
					
					String colName = results.getString(4);
					if(colName.equals("id")) {
						toFind.remove("id");
						
						type = results.getInt(5);
						if(type != Types.INTEGER) {
							errors.add("expected id to be an integer");
						}
						
						autoIncrement = results.getString(23);
						if(!autoIncrement.equals("YES")) {
							errors.add("expected id to autoincrement");
						}
					}else if(colName.equals("fullname")) {
						toFind.remove("fullname");
						
						type = results.getInt(5);
						if(type != Types.VARCHAR) {
							errors.add("expected fullname to be a varchar");
						}
					}else {
						errors.add("weird column " + colName);
					}
				}
				
				results.close();
				
				while(toFind.size() > 0) {
					String col = toFind.get(0);
					toFind.remove(0);
					
					errors.add(String.format("expected %s", col));
				}
				
				if(errors.size() > 0) {
					throw new RuntimeException("Validation failed on fullnames: " + errors.stream().collect(Collectors.joining("; ")));
				}
			}
			
		} catch (SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates the fullnames table
	 * 
	 * @throws SQLException
	 */
	protected void createFullnamesTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE fullnames ("
				+ "id int NOT NULL AUTO_INCREMENT, "
				+ "fullname varchar(50) NOT NULL, "
				+ "PRIMARY KEY(id))");
		statement.close();
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
