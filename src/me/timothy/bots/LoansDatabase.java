package me.timothy.bots;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.timothy.bots.models.CreationInfo;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Recheck;
import me.timothy.bots.models.Repayment;
import me.timothy.bots.models.ResetPasswordRequest;
import me.timothy.bots.models.Response;
import me.timothy.bots.models.ResponseHistory;
import me.timothy.bots.models.ShareCode;
import me.timothy.bots.models.User;

/**
 * Contains the connection to the MySQL database, both for remembering loans and
 * remembering reddit Thing fullnames so they are not parsed multiple times. <br>
 * <br>
 * This class connects with 1 database, but may be reused by multiple calls to
 * connect. Every public member function interacts with the underlying database.
 * This class is NOT thread-safe.
 * 
 * @author Timothy
 */
public class LoansDatabase extends Database {
	private Connection connection;
	
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
			connection.close();
		}
		connection = DriverManager.getConnection(url, username, password);
	}

	// ===========================================================
	// |                                                         |
	// |                       FULLNAMES                         |
	// |                                                         |
	// ===========================================================
	
	/*
	 * fullnames
	 *   id       - int primary key
	 *   fullname - varchar(50)
	 */
	
	
	/**
	 * Adds a fullname to the database
	 * @param id the fullname to add
	 */
	public void addFullname(String id) {
		PreparedStatement prep;
		try {
			prep = connection
					.prepareStatement("INSERT INTO fullnames (fullname) VALUES(?)");

			prep.setString(1, id);

			prep.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Scans the database for ids matching the specified id
	 * 
	 * @param id the id to scan for
	 * @return if the database has that id
	 */
	public boolean containsFullname(String id) {
		try {
			PreparedStatement prep = connection
					.prepareStatement("SELECT * FROM fullnames WHERE fullname=?");

			prep.setString(1, id);

			ResultSet results = prep.executeQuery();
			boolean hasFirst = results.first();
			results.close();

			return hasFirst;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	// ===========================================================
	// |                                                         |
	// |                         RECHECKS                        |
	// |                                                         |
	// ===========================================================
	
	/*
	 * rechecks 
	 *   id         - int
	 *   fullname   - string
	 *   
	 *   created_at - datetime
	 *   updated_at - datetime
	 */
	
	/**
	 * Gets all queued rechecks
	 * 
	 * @return any rechecks in the rechecks database
	 */
	public List<Recheck> getAllRechecks() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM rechecks");
			
			ResultSet set = statement.executeQuery();
			List<Recheck> results = new ArrayList<>();
			
			while(set.next()) {
				results.add(getRecheckFromSet(set));
			}
			
			set.close();
			return results;
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
	}
	
	/**
	 * Removes a recheck request (by id) from the database
	 * 
	 * @param recheck the recheck request to remove
	 */
	public void deleteRecheck(Recheck recheck) {
		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM rechecks WHERE id=?");
			statement.setInt(1, recheck.id);
			
			statement.executeUpdate();
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
	}
	
	/**
	 * Parases the recheck request thats currently selected
	 * in the result set
	 * 
	 * @param results the results
	 * @return the recheck currently selected
	 * @throws SQLException if a sql-exception occurs
	 */
	private Recheck getRecheckFromSet(ResultSet results) throws SQLException {
		return new Recheck(results.getInt("id"), results.getString("fullname"), results.getTimestamp("created_at"), results.getTimestamp("updated_at"));
	}
	
	// ===========================================================
	// |                                                         |
	// |                          USERS                          |
	// |                                                         |
	// ===========================================================

	/*
	 * users
	 *   id                 - int primary key
	 *   username           - varchar(255)
	 *   auth               - int
	 *   password_digest    - text
	 *   claimed            - tinyint(1)
	 *   claim_code         - varchar(255)
	 *   claim_link_sent_at - datetime
	 *   created_at         - datetime
	 *   updated_at         - datetime 
	 *   
	 *   email          - text
	 *   name           - text
	 *   street_address - text
	 *   city           - text
	 *   state          - text
	 *   zip            - text
	 *   country        - text
	 */
	
	/**
	 * Equivalent SQL: {@code SELECT * FROM users WHERE username=? LIMIT 1}
	 * 
	 * @param username the user to get
	 * @return that user or null
	 */
	public User getUserByUsername(String username) {
		try {
			PreparedStatement prep = connection
					.prepareStatement("SELECT * FROM users WHERE username=? LIMIT 1");
			prep.setString(1, username);
			ResultSet results = prep.executeQuery();
			
			if(results == null || !results.next())
				return null;
			
			User result = getUserFromSet(results);
			results.close();
			return result;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Equivalent SQL: {@code SELECT * FROM users WHERE id=? LIMIT 1}
	 * 
	 * @param id the user to get
	 * @return that user or null
	 */
	public User getUserById(int id) {
		try {
			PreparedStatement prep = connection
					.prepareStatement("SELECT * FROM users WHERE id=? LIMIT 1");
			prep.setInt(1, id);
			ResultSet results = prep.executeQuery();
			
			if(results == null || !results.next())
				return null;
			
			User result = getUserFromSet(results);
			results.close();
			return result;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets all users that are <i>unclaimed</i>, have a <i>claim code</i>, and
	 * do not have a <i>claim link sent at</i> date
	 * @return list of users to pm a code
	 */
	public List<User> getUsersToSendCode() {
		try {
			PreparedStatement prep = connection
					.prepareStatement("SELECT * FROM users WHERE (claimed=false OR claimed IS NULL) AND claim_code IS NOT NULL AND claim_link_sent_at IS NULL");
			ResultSet results = prep.executeQuery();
			
			List<User> result = new ArrayList<>();
			while(results.next()) {
				result.add(getUserFromSet(results));
			}
			
			results.close();
			return result;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the user by the specified username, or if it doesn't exist creates
	 * and saves a new user and returns that
	 * @param username the username of the user
	 * @return that user or a new one created with that username
	 */
	public User getOrCreateUserByUsername(String username) {
		User result = getUserByUsername(username);
		
		if(result == null) {
			result = new User(username);
			Date now = new Date();
			result.createdAt = new Timestamp(now.getTime());
			result.updatedAt = new Timestamp(now.getTime());
			addOrUpdateUser(result);
		}
		
		return result;
	}
	
	/**
	 * Adds the user if the id is <=0, which also updates the id
	 * to reflect the newly generated one. Otherwise just updates the
	 * user
	 * 
	 * @param user the user to add or update
	 * @throws IllegalArgumentException if the user is not valid
	 */
	public void addOrUpdateUser(User user) {
		if(!user.isValid())
			throw new IllegalArgumentException("invalid users cannot be saved");
		try {
			PreparedStatement statement;
			int counter = 1;
			if(user.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO users (username, auth, password_digest, claimed, claim_code, " +
						"claim_link_sent_at, created_at, updated_at, email, name, street_address, " +
						"city, state, zip, country) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE users SET username=?, auth=?, password_digest=?, claimed=?, claim_code=?, " +
						"claim_link_sent_at=?, created_at=?, updated_at=?, email=?, name=?, street_address=?, " +
						"city=?, state=?, zip=?, country=? WHERE id=?");
			}
			statement.setString(counter++, user.username);
			statement.setInt(counter++, user.auth);
			statement.setString(counter++, user.passwordDigest);
			statement.setBoolean(counter++, user.claimed);
			statement.setString(counter++, user.claimCode);
			statement.setTimestamp(counter++, user.claimLinkSetAt);
			statement.setTimestamp(counter++, user.createdAt);
			statement.setTimestamp(counter++, user.updatedAt);
			statement.setString(counter++, user.email);
			statement.setString(counter++, user.name);
			statement.setString(counter++, user.streetAddress);
			statement.setString(counter++, user.city);
			statement.setString(counter++, user.state);
			statement.setString(counter++, user.zip);
			statement.setString(counter++, user.country);
			if(user.id > 0)
				statement.setInt(counter++, user.id);
			
			statement.executeUpdate();
			
			if(user.id <= 0) {
				ResultSet set = statement.getGeneratedKeys();
				if(set.next())
					user.id = set.getInt(1);
				else
					throw new IllegalStateException("This can't be happening; no generated keys for table user?");
				set.close();
			}
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
	}

	/**
	 * Parses the user in the currently selected element of the set
	 * 
	 * @param set the set
	 * @return the user from that set
	 * @throws SQLException if a sql-exception occurs
	 */
	private User getUserFromSet(ResultSet set) throws SQLException {
		User user = new User(set.getInt("id"), set.getString("username"), set.getInt("auth"),
				set.getString("password_digest"), set.getBoolean("claimed"), 
				set.getString("claim_code"), set.getTimestamp("claim_link_sent_at"),
				set.getTimestamp("created_at"), set.getTimestamp("updated_at"),
				set.getString("email"), set.getString("name"), set.getString("street_address"),
				set.getString("city"), set.getString("state"), set.getString("zip"), 
				set.getString("country"));
		
		if(user.createdAt == null)
			user.createdAt = new Timestamp(System.currentTimeMillis());
		if(user.updatedAt == null)
			user.updatedAt = new Timestamp(System.currentTimeMillis());
		
		return user;
	}
	
	// ===========================================================
	// |                                                         |
	// |                          LOANS                          |
	// |                                                         |
	// ===========================================================
	
	/*
	 * loans
	 *   id                        - int primary key
	 *   lender_id                 - int mul
	 *   borrower_id               - int mul
	 *   principal_cents           - int
	 *   principal_repayment_cents - int
	 *   unpaid                    - tinyint(1)
	 *   deleted                   - tinyint(1)
	 *   deleted_reason            - text
	 *   created_at                - datetime
	 *   updated_at                - datetime
	 *   deleted_at                - datetime
	 */

	/**
	 * Gets loans matching the specified requirements
	 * 
	 * @param borrowerId the borrower id
	 * @param lenderId the lender id
	 * @param strict if both lender and borrower have to match, or just either
	 * @return the matching loans (potentially empty, never null)
	 */
	public List<Loan> getLoansWithBorrowerAndOrLender(int borrowerId, int lenderId, boolean strict) {
		List<Loan> results = new ArrayList<>();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM loans WHERE deleted=0 AND (lender_id=? " + (strict ? "AND" : "OR") + " borrower_id=?)");
			statement.setInt(1, lenderId);
			statement.setInt(2, borrowerId);
			ResultSet set = statement.executeQuery();
			while(set.next()) {
				results.add(getLoanFromSet(set));
			}
			set.close();
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
		return results;
	}
	
	/**
	 * Add and give an id to loans with id <=0, otherwise updates the loan
	 * 
	 * @param loan the loan to add or update
	 */
	public void addOrUpdateLoan(Loan loan) {
		if(!loan.isValid())
			throw new IllegalArgumentException("invalid loans cannot be saved");
		
		try {
			PreparedStatement statement;
			int counter = 1;
			if(loan.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO loans (lender_id, borrower_id, " +
						"principal_cents, principal_repayment_cents, unpaid, deleted, deleted_reason, " +
						"created_at, updated_at, deleted_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE loans SET lender_id=?, borrower_id=?, principal_cents=?, principal_repayment_cents=?, " +
						"unpaid=?, deleted=?, deleted_reason=?, created_at=?, updated_at=?, deleted_at=? WHERE id=?");
			}
			
			statement.setInt(counter++, loan.lenderId);
			statement.setInt(counter++, loan.borrowerId);
			statement.setInt(counter++, loan.principalCents);
			statement.setInt(counter++, loan.principalRepaymentCents);
			statement.setBoolean(counter++, loan.unpaid);
			statement.setBoolean(counter++, loan.deleted);
			statement.setString(counter++, loan.deletedReason);
			statement.setTimestamp(counter++, loan.createdAt);
			statement.setTimestamp(counter++, loan.updatedAt);
			statement.setTimestamp(counter++, loan.deletedAt);
			
			if(loan.id > 0)
				statement.setInt(counter++, loan.id);
			
			statement.executeUpdate();
			
			if(loan.id <= 0) {
				ResultSet set = statement.getGeneratedKeys();
				if(set.next())
					loan.id = set.getInt(1);
				else
					throw new IllegalStateException("This can't be happening; no generated keys for table loans?");
				set.close();
			}
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
	}

	/**
	 * Creates a repayment with the specified amount, saves it, and updates the specified
	 * loan both in memory and in the database while updating the updatedAt for the loan
	 * <br><br>
	 * This WILL fail at the database level if the loan is not yet saved, since the repayment
	 * is saved prior to the loan being updated, and the loan id is required to make the repayment
	 * 
	 * @param loan the loan to update
	 * @param amount amount in pennies
	 * @param time the timestamp to use
	 */
	public void payLoan(Loan loan, int amount, long time) {
		Repayment repayment = new Repayment(-1, loan.id, amount, new Timestamp(time), new Timestamp(time));
		addRepayment(repayment);
		
		loan.principalRepaymentCents += amount;
		loan.updatedAt = new Timestamp(time);
		addOrUpdateLoan(loan);
	}
	
	/**
	 * Sets the specified loan unpaidness, both in the database and in memory
	 * @param loan the loan to change
	 * @param unpaid to set or not to set, that is the question
	 */
	public void setLoanUnpaid(Loan loan, boolean unpaid) {
		try {
			PreparedStatement statement = connection.prepareStatement("UPDATE loans SET unpaid=?, updated_at=? WHERE id=?");
			statement.setBoolean(1, unpaid);
			statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			statement.setInt(3, loan.id);
			
			statement.executeUpdate();
			
			loan.unpaid = unpaid;
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
	}
	
	/**
	 * Gets the loan from the currently selected row in the set
	 * @param set the set to parse from
	 * @return the Java-representation of the row
	 * @throws SQLException if a sql-exception occurs
	 */
	private Loan getLoanFromSet(ResultSet set) throws SQLException {
		return new Loan(set.getInt("id"), set.getInt("lender_id"), set.getInt("borrower_id"), set.getInt("principal_cents"), 
				set.getInt("principal_repayment_cents"), set.getBoolean("unpaid"), set.getBoolean("deleted"),
				set.getString("deleted_reason"), set.getTimestamp("created_at"), set.getTimestamp("updated_at"),
				set.getTimestamp("deleted_at"));
	}
	
	// ===========================================================
	// |                                                         |
	// |                      CREATION_INFO                      |
	// |                                                         |
	// ===========================================================
	
	/*
	 * creation_infos
	 *   id         - int primary key
	 *   loan_id    - int mul
	 *   type       - int not null
	 *   
	 *   -- type 0; reddit --
	 *   thread     - text
	 *   
	 *   -- type 1; redditloans admin --
	 *   reason     - text
	 *   user_id    - int mul
	 *   
	 *   created_at - datetime
	 *   updated_at - datetime
	 */
	
	/**
	 * If the infos id is unset (<= 0) then inserts it into the database
	 * and sets the id to the generated key.
	 * <br><br>
	 * If the infos id is set (> 0) then updates the info in the database
	 * 
	 * @param info the info to insert or update
	 */
	public void addOrUpdateCreationInfo(CreationInfo info) {
		if(!info.isValid())
			throw new RuntimeException("Cannot add or update invalid creation infos");
		
		try {
			PreparedStatement statement = null;
			
			if(info.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO creation_infos (loan_id, type, thread, reason, " +
						"user_id, created_at, updated_at) VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE creation_infos SET loan_id=?, type=?, thread=?, reason=?, user_id=?, " +
						"created_at=?, updated_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, info.loanId);
			statement.setInt(counter++, info.type.getTypeNum());
			statement.setString(counter++, info.thread);
			statement.setString(counter++, info.reason);
			if(info.userId == -1)
				statement.setNull(counter++, Types.INTEGER);
			else
				statement.setInt(counter++, info.userId);
			statement.setTimestamp(counter++, info.createdAt);
			statement.setTimestamp(counter++, info.updatedAt);
			
			if(info.id > 0) 
				statement.setInt(counter++, info.id);
			
			statement.executeUpdate();
			
			if(info.id <= 0) {
				ResultSet set = statement.getGeneratedKeys();
				if(set.next())
					info.id = set.getInt(1);
				else
					throw new IllegalStateException("This can't be happening; no generated keys for table creation_infos?");
				set.close();
			}
		}catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Get the creation info with the specified id
	 * @param id the id
	 * @return the creation info with that id if it exists, null otherwise
	 */
	public CreationInfo getCreationInfoByLoanId(int loanId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM creation_infos WHERE loan_id=? LIMIT 1");
			statement.setInt(1, loanId);
			
			ResultSet set = statement.executeQuery();
			
			CreationInfo result = (set.next() ? getCreationInfoFromSet(set) : null);
			
			set.close();
			return result;
		}catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private CreationInfo getCreationInfoFromSet(ResultSet set) throws SQLException {
		return new CreationInfo(
				set.getInt("id"),
				set.getInt("loan_id"),
				CreationInfo.CreationType.getByTypeNum(set.getInt("type")),
				set.getString("thread"),
				set.getString("reason"),
				set.getInt("user_id"),
				set.getTimestamp("created_at"),
				set.getTimestamp("updated_at")
				);
	}
	// ===========================================================
	// |                                                         |
	// |                        REPAYMENTS                       |
	// |                                                         |
	// ===========================================================
	
	/*
	 * repayments
	 *   id           - int primary key
	 *   loan_id      - int mul
	 *   amount_cents - int
	 *   created_at   - datetime
	 *   updated_at   - datetime
	 *   
	 */
	
	/**
	 * Gets all repayments attached to the specified loan
	 * @param loanId the loan id
	 * @return a (potentially empty) list of repayments for the loan
	 */
	public List<Repayment> getRepaymentsWithLoan(int loanId) {
		List<Repayment> result = new ArrayList<>();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM repayments WHERE loan_id=?");
			statement.setInt(1, loanId);
			
			ResultSet set = statement.executeQuery();
			while(set.next()) {
				result.add(getRepaymentFromSet(set));
			}
			set.close();
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
		return result;
	}
	
	/**
	 * Adds the specified repayment to the database. Does not
	 * update the associated loan. Does update the repayment memory
	 * objects id
	 * 
	 * @param repayment the repayment to add
	 */
	public void addRepayment(Repayment repayment) {
		if(!repayment.isValid())
			throw new IllegalArgumentException("invalid repayments cannot be saved");
		
		try {
			PreparedStatement statement;
			int counter = 1;
			statement = connection.prepareStatement("INSERT INTO repayments (loan_id, amount_cents, " +
					"created_at, updated_at) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			
			statement.setInt(counter++, repayment.loanId);
			statement.setInt(counter++, repayment.amountCents);
			statement.setTimestamp(counter++, repayment.createdAt);
			statement.setTimestamp(counter++, repayment.updatedAt);
			
			statement.executeUpdate();
			
			ResultSet set = statement.getGeneratedKeys();
			if(set.next())
				repayment.id = set.getInt(1);
			else
				throw new IllegalStateException("This can't be happening; no generated keys for table repayments?");
			set.close();
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
	}
	
	/**
	 * Gets a repayment from the currently selected row in the set
	 * @param set the set
	 * @return the repayment
	 * @throws SQLException if a sql-exception occurs
	 */
	private Repayment getRepaymentFromSet(ResultSet set) throws SQLException {
		return new Repayment(set.getInt("id"), set.getInt("loan_id"), set.getInt("amount_cents"), set.getTimestamp("created_at"), set.getTimestamp("updated_at"));
	}
	
	// ===========================================================
	// |                                                         |
	// |                       SHARE CODES                       |
	// |                                                         |
	// ===========================================================
	
	/*
	 * share_codes
	 *   id        -  int primary key
	 *   user_id    - int mul
	 *   code       - varchar(255)
	 *   created_at - datetime
	 *   updated_at - datetime
	 */
	
	/**
	 * Gets all share codes attached to the specified user (expected only 1)
	 * @param userId the user
	 * @return all attached sharecodes (or an empty list if no attached sharecodes)
	 */
	public List<ShareCode> getShareCodesForUser(int userId) {
		List<ShareCode> result = new ArrayList<>();
		
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM share_codes WHERE user_id=?");
			statement.setInt(1, userId);
			ResultSet set = statement.executeQuery();
			while(set.next()) {
				result.add(getShareCodeFromSet(set));
			}
			set.close();
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
		
		return result;
	}
	
	/**
	 * The equivalent of {@code DELETE FROM share_codes WHERE id=? LIMIT 1}
	 * @param id the share code id to delete
	 */
	public void deleteCode(int id) {
		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM share_codes WHERE id=? LIMIT 1");
			statement.setInt(1, id);
			statement.executeUpdate();
		}catch(SQLException sqlE) {
			throw new RuntimeException(sqlE);
		}
	}
	
	/**
	 * Gets the share code from the set 
	 * @param set the set
	 * @return the share code
	 * @throws SQLException if a sql-exception occurs
	 */
	private ShareCode getShareCodeFromSet(ResultSet set) throws SQLException {
		return new ShareCode(set.getInt("id"), set.getInt("user_id"), set.getString("code"), set.getTimestamp("created_at"), set.getTimestamp("updated_at"));
	}
	
	// ===========================================================
	// |                                                         |
	// |                        RESPONSES                        |
	// |                                                         |
	// ===========================================================
	
	/*
	 * responses
	 *   id            - int primary key
	 *   name          - varchar(255)
	 *   response_body - text
	 *   created_at    - datetime
	 *   updated_at    - datetime
	 */
	
	/**
	 * Adds or updates the response, assuming it is valid.
	 * The results id will be updated if it is <= 0, since
	 * that implies its a new response.
	 * 
	 * @param response the response
	 */
	public void addOrUpdateResponse(Response response) {
		if(!response.isValid()) 
			throw new IllegalArgumentException("Invalid responses cannot be saved!");
		
		try {
			PreparedStatement statement = null;
			if(response.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO responses (name, response_body, created_at, updated_at) " +
						"VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE responses SET name=?, response_body=?, created_at=?, updated_at=? WHERE id=?");
			}
			int counter = 1;
			
			statement.setString(counter++, response.name);
			statement.setString(counter++, response.responseBody);
			statement.setTimestamp(counter++, response.createdAt);
			statement.setTimestamp(counter++, response.updatedAt);
			
			statement.executeUpdate();
			
			if(response.id <= 0) {
				ResultSet set = statement.getGeneratedKeys();
				if(set.next())
					response.id = set.getInt(1);
				else
					throw new IllegalStateException("This can't be happening; no generated keys for table responses?");
				set.close();
			}
		}catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Gets the response from the database by name if it exists,
	 * or null otherwise
	 * 
	 * @param name name to search for
	 * @return the response, or null
	 * @throws RuntimeException as a wrapper for SQLExceptions
	 */
	public Response getResponseByName(String name) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM responses WHERE name LIKE ? LIMIT 1");
			statement.setString(1, name);
			
			ResultSet results = statement.executeQuery();
			if(!results.first()) {
				results.close();
				return null;
			}
			Response response = getResponseFromSet(results);
			results.close();
			return response;
		}catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	/**
	 * Gets the response from the set
	 * @param set the set
	 * @return the response
	 * @throws SQLException if a sql-exception occurs
	 */
	private Response getResponseFromSet(ResultSet set) throws SQLException {
		return new Response(set.getInt("id"), set.getString("name"), set.getString("response_body"), set.getTimestamp("created_at"), set.getTimestamp("updated_at"));
	}
	
	// ===========================================================
	// |                                                         |
	// |                    RESPONSE HISTORIES                   |
	// |                                                         |
	// ===========================================================
	
	/*
	 * response_histories
	 *   id          - int primary key
	 *   response_id - int mul
	 *   user_id     - int mul
	 *   old_raw     - text 
	 *   new_raw     - text
	 *   reason      - text
	 *   created_at  - datetime
	 *   updated_at  - datetime
	 */
	
	/**
	 * Get the response history from the id of the
	 * response
	 * @param responseId the response id
	 * @return a list (potentially empty) of history to the response
	 */
	public List<ResponseHistory> getResponseHistory(int responseId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM response_histories WHERE response_id=?");
			statement.setInt(1, responseId);
			
			ResultSet set = statement.executeQuery();
			
			List<ResponseHistory> result = new ArrayList<>();
			
			while(set.next()) {
				result.add(getResponseHistoryFromSet(set));
			}
			
			set.close();
			
			return result;
		}catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Get the response history from the set
	 * @param set the set
	 * @return the response history
	 * @throws SQLException if a sql-exception occurs
	 */
	private ResponseHistory getResponseHistoryFromSet(ResultSet set) throws SQLException {
		return new ResponseHistory(set.getInt("id"), set.getInt("response_id"), set.getInt("user_id"), set.getString("old_raw"),
				set.getString("new_raw"), set.getString("reason"), set.getTimestamp("created_at"), set.getTimestamp("updated_at"));
	}
	
	// ===========================================================
	// |                                                         |
	// |                 RESET PASSWORD REQUESTS                 |
	// |                                                         |
	// ===========================================================
	
	/*
	 * reset_password_requests
	 *   id              - int primary key 
	 *   user_id         - int mul
	 *   reset_code      - varchar(255)
	 *   reset_code_sent - tinyint(1)
	 *   reset_code_used - tinyint(1)
	 *   created_at      - datetime
	 *   updated_at      - datetime
	 */
	
	/**
	 * Gets a list of all current reset password requests that have not
	 * yet been sent out.
	 *
	 * @return all reset password requests where reset_code_sent=0. Not null, potentially empty
	 */
	public List<ResetPasswordRequest> getUnsentResetPasswordRequests() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM reset_password_requests WHERE reset_code_sent=0");
			
			ResultSet set = statement.getResultSet();
			List<ResetPasswordRequest> result = new ArrayList<>();
			while(set.next()) {
				result.add(getResetPasswordRequestFromSet(set));
			}
			set.close();
			return result;
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Saves a reset password request to the database, if and only 
	 * if the request is valid.
	 * 
	 * @param request the request to save
	 * @throws IllegalArgumentException if the request is not valid
	 */
	public void updateResetPasswordRequest(ResetPasswordRequest request) throws IllegalArgumentException {
		if(!request.isValid()) {
			throw new IllegalArgumentException("Request is not valid");
		}
		try {
			PreparedStatement statement = connection.prepareStatement("UPDATE reset_password_requests SET user_id=?, reset_code=?, " +
					"reset_code_sent=?, reset_code_used=?, created_at=?, updated_at=? WHERE id=?");
			
			int counter = 1;
			statement.setInt(counter++, request.userId);
			statement.setString(counter++, request.resetCode);
			statement.setBoolean(counter++, request.resetCodeSent);
			statement.setBoolean(counter++, request.resetCodeUsed);
			statement.setTimestamp(counter++, request.createdAt);
			statement.setTimestamp(counter++, request.updatedAt);
			
			statement.setInt(counter++, request.id);
			
			statement.executeUpdate();
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Gets the reset password request from the set 
	 * @param set the set
	 * @return the reset password request
	 * @throws SQLException if a sql-exception occurs
	 */
	private ResetPasswordRequest getResetPasswordRequestFromSet(ResultSet set) throws SQLException {
		return new ResetPasswordRequest(set.getInt("id"), set.getInt("user_id"), set.getString("reset_code"),
				set.getBoolean("reset_code_sent"), set.getBoolean("reset_code_used"), set.getTimestamp("created_at"),
				set.getTimestamp("updated_at"));
	}
}
