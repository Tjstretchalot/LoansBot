package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.UsernameMapping;
import me.timothy.bots.models.Username;

public class MysqlUsernameMapping extends MysqlObjectMapping<Username> implements UsernameMapping {
	private static final Logger logger = LogManager.getLogger();
	public MysqlUsernameMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "usernames", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.VARCHAR, "username"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(Username a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException("invalid username " + a);
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 1) {
				statement = connection.prepareStatement("UPDATE usernames SET user_id=?, username=?, created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO usernames (user_id, username, created_at, updated_at) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.userId);
			statement.setString(counter++, a.username);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.updatedAt);
			
			if(a.id > 1) {
				statement.setInt(counter++, a.id);
				statement.execute();
			}else {
				statement.execute();
				
				ResultSet keys = statement.getGeneratedKeys();
				if(keys.next()) {
					a.id = keys.getInt(1);
				}else {
					keys.close();
					statement.close();
					throw new RuntimeException("No generated keys for table username?");
				}
				keys.close();
			}
			statement.close();
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Username fetchById(int usernameId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM usernames WHERE id=?");
			statement.setInt(1, usernameId);
			
			ResultSet results = statement.executeQuery();
			Username username = null;
			if(results.next()) {
				username = fetchFromSet(results);
			}
			results.close();
			
			statement.close();
			return username;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<Username> fetchByUserId(int userId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM usernames WHERE user_id=?");
			statement.setInt(1, userId);
			
			ResultSet results = statement.executeQuery();
			List<Username> usernames = new ArrayList<>();
			while(results.next()) {
				usernames.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return usernames;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Username fetchByUsername(String usernameStr) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM usernames WHERE username=?");
			statement.setString(1, usernameStr);
			
			ResultSet results = statement.executeQuery();
			Username username = null;
			if(results.next()) {
				username = fetchFromSet(results);
			}
			results.close();
			
			statement.close();
			return username;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<Username> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM usernames");
			
			ResultSet results = statement.executeQuery();
			List<Username> usernames = new ArrayList<>();
			while(results.next()) {
				usernames.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return usernames;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE usernames ("
				+ "id int NOT NULL AUTO_INCREMENT, "
				+ "user_id int, "
				+ "username varchar(255), "
				+ "created_at timestamp NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_uname_user_id (user_id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

	/**
	 * Fetches the username in the current row of the ResultSet
	 * @param set the result set to fetch the usernaem from
	 * @return the username in the current row of the ResultSet
	 * @throws SQLException if one occurs
	 */
	protected Username fetchFromSet(ResultSet set) throws SQLException {
		return new Username(set.getInt("id"), set.getInt("user_id"), set.getString("username"),
				set.getTimestamp("created_at"), set.getTimestamp("updated_at"));
	}
}
