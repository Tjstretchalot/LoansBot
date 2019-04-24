package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.UserMapping;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;

public class MysqlUserMapping extends MysqlObjectMapping<User> implements UserMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlUserMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "users", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "auth"),
				new MysqlColumn(Types.LONGVARCHAR, "password_digest"),
				new MysqlColumn(Types.BIT, "claimed"),
				new MysqlColumn(Types.VARCHAR, "claim_code"),
				new MysqlColumn(Types.TIMESTAMP, "claim_link_sent_at"),
				new MysqlColumn(Types.LONGVARCHAR, "email"),
				new MysqlColumn(Types.LONGVARCHAR, "name"),
				new MysqlColumn(Types.LONGVARCHAR, "street_address"),
				new MysqlColumn(Types.LONGVARCHAR, "city"),
				new MysqlColumn(Types.LONGVARCHAR, "state"),
				new MysqlColumn(Types.LONGVARCHAR, "zip"),
				new MysqlColumn(Types.LONGVARCHAR, "country"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(User a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException("Invalid user: " + a);
		
		// Nanoseconds can't be saved in MySQL - having nanoseconds won't cause
		// errors, but it won't match what's in the database
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		if(a.claimLinkSentAt != null) { a.claimLinkSentAt.setNanos(0); } 
		
		try {
			PreparedStatement statement;
			
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE users SET auth=?, password_digest=?, claimed=?, "
						+ "claim_code=?, claim_link_sent_at=?, email=?, name=?, street_address=?, city=?, "
						+ "state=?, zip=?, country=?, created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO users (auth, password_digest, "
						+ "claimed, claim_code, claim_link_sent_at, email, name, street_address, city, "
						+ "state, zip, country, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, "
						+ "?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.auth);
			statement.setString(counter++, a.passwordDigest);
			statement.setBoolean(counter++, a.claimed);
			statement.setString(counter++, a.claimCode);
			statement.setTimestamp(counter++, a.claimLinkSentAt);
			statement.setString(counter++, a.email);
			statement.setString(counter++, a.name);
			statement.setString(counter++, a.streetAddress);
			statement.setString(counter++, a.city);
			statement.setString(counter++, a.state);
			statement.setString(counter++, a.zip);
			statement.setString(counter++, a.country);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.updatedAt);
			
			if(a.id > 0) {
				statement.setInt(counter++, a.id);
				statement.execute();
			}else {
				statement.execute();
				ResultSet keys = statement.getGeneratedKeys();
				if(!keys.next()) {
					keys.close();
					statement.close();
					throw new IllegalStateException("Expected generated keys!");
				}
				a.id = keys.getInt(1);
				keys.close();
			}
			statement.close();
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public User fetchById(int id) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id=?");
			statement.setInt(1, id);
			
			ResultSet results = statement.executeQuery();
			User user = null;
			if(results.next()) {
				user = fetchFromSet(results);
			}
			results.close();
			statement.close();
			return user;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public User fetchOrCreateByName(String usernameStr) {
		Username username = database.getUsernameMapping().fetchByUsername(usernameStr);
		
		if(username == null) {
			User user = new User();
			user.id = -1;
			user.createdAt = new Timestamp(System.currentTimeMillis());
			user.updatedAt = new Timestamp(System.currentTimeMillis());
			save(user);
			
			
			username = new Username(-1, user.id, usernameStr, new Timestamp(System.currentTimeMillis()),
					new Timestamp(System.currentTimeMillis()));
			database.getUsernameMapping().save(username);
			
			return user;
		}
		
		return fetchById(username.userId);
	}

	@Override
	public int fetchMaxUserId() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT MAX(id) FROM users");
			ResultSet results = statement.executeQuery();
			
			int result = -1;
			if(results.next()) {
				result = results.getInt(1);
			}
			results.close();
			statement.close();
			return result;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<User> fetchUsersToSendCode() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE claimed=0 "
					+ "AND claim_code IS NOT NULL AND claim_link_sent_at IS NULL");
			ResultSet results = statement.executeQuery();
			
			List<User> users = new ArrayList<>();
			while(results.next()) {
				users.add(fetchFromSet(results));
			}
			
			results.close();
			statement.close();
			return users;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<User> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM users");
			ResultSet results = statement.executeQuery();
			
			List<User> users = new ArrayList<>();
			while(results.next()) {
				users.add(fetchFromSet(results));
			}
			
			results.close();
			statement.close();
			return users;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE users ("
				+ "id int NOT NULL AUTO_INCREMENT, "
				+ "auth int NOT NULL, "
				+ "password_digest text, "
				+ "claimed tinyint(1), "
				+ "claim_code varchar(255), "
				+ "email text, "
				+ "name text, "
				+ "street_address text, "
				+ "city text, "
				+ "state text, "
				+ "zip text, "
				+ "country text, "
				+ "claim_link_sent_at timestamp NULL DEFAULT '1970-01-01 00:00:01', "
				+ "created_at timestamp NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (id)"
				+ ")");
		statement.close();
	}

	/**
	 * Fetches the user in the current row of the set
	 * @param set the set to get the user from
	 * @return the user in the current row of the set
	 * @throws SQLException if one occurs
	 */
	protected User fetchFromSet(ResultSet set) throws SQLException {
		return new User(set.getInt("id"), set.getInt("auth"), set.getString("password_digest"), set.getBoolean("claimed"), 
				set.getString("claim_code"), set.getTimestamp("claim_link_sent_at"), set.getTimestamp("created_at"), 
				set.getTimestamp("updated_at"), set.getString("email"), set.getString("name"), set.getString("street_address"),
				set.getString("city"), set.getString("state"), set.getString("zip"), set.getString("country"));
	}
}
