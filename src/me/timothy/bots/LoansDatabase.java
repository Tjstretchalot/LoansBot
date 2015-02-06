package me.timothy.bots;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.timothy.bots.models.Loan;
import me.timothy.bots.models.Recheck;
import me.timothy.bots.models.Repayment;
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
	 *   id - int primary key
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
	 *   id - int
	 *   fullname - string
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
	 *   id - int primary key
	 *   username - varchar(255)
	 *   auth - int
	 *   password_digest - text
	 *   claimed - tinyint(1)
	 *   claim_code - varchar(255)
	 *   claim_link_sent_at - datetime
	 *   created_at - datetime
	 *   updated_at - datetime 
	 *   
	 *   email - text
	 *   name - text
	 *   street_address - text
	 *   city - text
	 *   state - text
	 *   zip - text
	 *   country - text
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
	 * Adds the user if the id is -1, which also updates the id
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
			if(user.id == -1) {
				statement = connection.prepareStatement("INSERT INTO users (username, auth, password_digest, claimed, claim_code, " +
						"claim_link_sent_at, created_at, updated_at, email, name, street_address, " +
						"city, state, zip, country) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
			if(user.id != -1)
				statement.setInt(counter++, user.id);
			
			statement.executeUpdate();
			
			if(user.id == -1) {
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
	 *   id - int primary key
	 *   lender_id - int mul
	 *   borrower_id - int mul
	 *   principal_cents - int
	 *   principal_repayment_cents - int
	 *   unpaid - tinyint(1)
	 *   
	 *   original_thread - text
	 *   
	 *   created_at - datetime
	 *   updated_at - datetime
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
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM loans WHERE lender_id=? " + (strict ? "AND" : "OR") + " borrower_id=?");
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
	 * Add and give an id to loans with id -1, otherwise updates the loan
	 * 
	 * @param loan the loan to add or update
	 */
	public void addOrUpdateLoan(Loan loan) {
		if(!loan.isValid())
			throw new IllegalArgumentException("invalid loans cannot be saved");
		
		try {
			PreparedStatement statement;
			int counter = 1;
			if(loan.id == -1) {
				statement = connection.prepareStatement("INSERT INTO loans (lender_id, borrower_id, " +
						"principal_cents, principal_repayment_cents, unpaid, original_thread, created_at, updated_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE loans SET lender_id=?, borrower_id=?, principal_cents=?, principal_repayment_cents=?, " +
						"unpaid=?, original_thread=?, created_at=?, updated_at=? WHERE id=?");
			}
			
			statement.setInt(counter++, loan.lenderId);
			statement.setInt(counter++, loan.borrowerId);
			statement.setInt(counter++, loan.principalCents);
			statement.setInt(counter++, loan.principalRepaymentCents);
			statement.setBoolean(counter++, loan.unpaid);
			statement.setString(counter++, loan.originalThread);
			statement.setTimestamp(counter++, loan.createdAt);
			statement.setTimestamp(counter++, loan.updatedAt);
			
			if(loan.id != -1)
				statement.setInt(counter++, loan.id);
			
			statement.executeUpdate();
			
			if(loan.id == -1) {
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
			PreparedStatement statement = connection.prepareStatement("UPDATE loans SET unpaid=? WHERE id=?");
			statement.setBoolean(1, unpaid);
			statement.setInt(2, loan.id);
			
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
				set.getInt("principal_repayment_cents"), set.getBoolean("unpaid"), set.getString("original_thread"), set.getTimestamp("created_at"), 
				set.getTimestamp("updated_at"));
	}
	
	// ===========================================================
	// |                                                         |
	// |                        REPAYMENTS                       |
	// |                                                         |
	// ===========================================================
	
	/*
	 * repayments
	 *   id - int primary key
	 *   loan_id - int mul
	 *   amount_cents - int
	 *   created_at - datetime
	 *   updated_at - datetime
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
	 *   id - int primary key
	 *   user_id - int mul
	 *   code - varchar(255)
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
}
