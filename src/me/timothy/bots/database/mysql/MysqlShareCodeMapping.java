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
import me.timothy.bots.database.ShareCodeMapping;
import me.timothy.bots.models.ShareCode;

public class MysqlShareCodeMapping extends MysqlObjectMapping<ShareCode> implements ShareCodeMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlShareCodeMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "share_codes",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.VARCHAR, "code"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(ShareCode a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new RuntimeException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE share_codes SET "
						+ "user_id=?, code=?, created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO share_codes "
						+ "(user_id, code, created_at, updated_at) VALUES (?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.userId);
			statement.setString(counter++, a.code);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.updatedAt);
			
			if(a.id > 0) {
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
					throw new RuntimeException("Expected generated keys for share_codes, but didn't get any!");
				}
				keys.close();
			}
			statement.close();
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ShareCode> fetchForUser(int userId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM share_codes WHERE user_id=?");
			statement.setInt(1, userId);
			
			ResultSet results = statement.executeQuery();
			List<ShareCode> shareCodes = new ArrayList<>();
			while(results.next()) {
				shareCodes.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return shareCodes;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(ShareCode shareCode) {
		if(shareCode.id < 0) {
			throw new IllegalArgumentException("Deletion is done through id, so " + shareCode + " is not valid");
		}
		
		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM share_codes WHERE id=?");
			statement.setInt(1, shareCode.id);
			statement.execute();
			statement.close();
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ShareCode> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM share_codes");
			
			ResultSet results = statement.executeQuery();
			List<ShareCode> shareCodes = new ArrayList<>();
			while(results.next()) {
				shareCodes.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return shareCodes;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Fetches the ShareCode in the current row of the ResultSet
	 * @param results the result set
	 * @return the ShareCode in the current row
	 * @throws SQLException if one occurs
	 */
	protected ShareCode fetchFromSet(ResultSet results) throws SQLException {
		return new ShareCode(results.getInt("id"), results.getInt("user_id"), 
				results.getString("code"), results.getTimestamp("created_at"), 
				results.getTimestamp("updated_at"));
	}
	
	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE share_codes ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "user_id INT NOT NULL, "
				+ "code VARCHAR(255) NOT NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_sc_user_id (user_id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")");
		
	}
}
