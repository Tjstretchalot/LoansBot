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
import me.timothy.bots.database.ResponseHistoryMapping;
import me.timothy.bots.models.ResponseHistory;

public class MysqlResponseHistoryMapping extends MysqlObjectMapping<ResponseHistory> implements ResponseHistoryMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlResponseHistoryMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "response_histories",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "response_id"),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.LONGVARCHAR, "old_raw"),
				new MysqlColumn(Types.LONGVARCHAR, "new_raw"),
				new MysqlColumn(Types.LONGVARCHAR, "reason"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(ResponseHistory a) throws IllegalArgumentException {
		if(!a.isValid()) 
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE response_histories SET "
						+ "response_id=?, user_id=?, old_raw=?, new_raw=?, reason=?, created_at=?, "
						+ "updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO response_histories "
						+ "(response_id, user_id, old_raw, new_raw, reason, created_at, updated_at) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.responseId);
			statement.setInt(counter++, a.userId);
			statement.setString(counter++, a.oldRaw);
			statement.setString(counter++, a.newRaw);
			statement.setString(counter++, a.reason);
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
					throw new RuntimeException("expected response_histories to return generated keys, but didn't get any!");
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
	public List<ResponseHistory> fetchForResponse(int responseId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM response_histories WHERE response_id=?");
			statement.setInt(1, responseId);
			
			ResultSet results = statement.executeQuery();
			List<ResponseHistory> rhs = new ArrayList<>();
			while(results.next()) {
				rhs.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return rhs;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ResponseHistory> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM response_histories");
			
			ResultSet results = statement.executeQuery();
			List<ResponseHistory> rhs = new ArrayList<>();
			while(results.next()) {
				rhs.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return rhs;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Fetches the response history in the current row in the result set
	 * @param results the result set 
	 * @return the response history in the current row
	 * @throws SQLException if one occurs
	 */
	protected ResponseHistory fetchFromSet(ResultSet results) throws SQLException {
		return new ResponseHistory(results.getInt("id"), results.getInt("response_id"), results.getInt("user_id"), 
				results.getString("old_raw"), results.getString("new_raw"), results.getString("reason"), 
				results.getTimestamp("created_at"), results.getTimestamp("updated_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE response_histories ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "response_id INT NOT NULL, "
				+ "user_id INT NOT NULL, "
				+ "old_raw TEXT, "
				+ "new_raw TEXT NOT NULL, "
				+ "reason TEXT NOT NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_rehi_response_id (response_id), "
				+ "INDEX ind_rehi_user_id (user_id), "
				+ "FOREIGN KEY (response_id) REFERENCES responses(id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
