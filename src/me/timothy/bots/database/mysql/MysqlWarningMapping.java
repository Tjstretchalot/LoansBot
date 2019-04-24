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
import me.timothy.bots.database.WarningMapping;
import me.timothy.bots.models.Warning;

public class MysqlWarningMapping extends MysqlObjectMapping<Warning> implements WarningMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlWarningMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "warnings",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "warned_user_id"),
				new MysqlColumn(Types.INTEGER, "warning_user_id"),
				new MysqlColumn(Types.LONGVARCHAR, "violation"),
				new MysqlColumn(Types.LONGVARCHAR, "action_taken"),
				new MysqlColumn(Types.LONGVARCHAR, "next_action"),
				new MysqlColumn(Types.LONGVARCHAR, "notes"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(Warning a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new RuntimeException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE warnings SET warned_user_id=?, "
						+ "warning_user_id=?, violation=?, action_taken=?, next_action=?, notes=?, "
						+ "created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO warnings (warned_user_id, "
						+ "warning_user_id, violation, action_taken, next_action, notes, created_at, "
						+ "updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.warnedUserId);
			statement.setInt(counter++, a.warningUserId);
			statement.setString(counter++, a.violation);
			statement.setString(counter++, a.actionTaken);
			statement.setString(counter++, a.nextAction);
			statement.setString(counter++, a.notes);
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
					throw new RuntimeException("Expected generated keys from warnings but didn't get any!");
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
	public List<Warning> fetchByWarnedUserId(int userId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM warnings WHERE warned_user_id=?");
			statement.setInt(1, userId);
			
			ResultSet results = statement.executeQuery();
			List<Warning> warnings = new ArrayList<>();
			while(results.next()) {
				warnings.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return warnings;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Warning> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM warnings");
			
			ResultSet results = statement.executeQuery();
			List<Warning> warnings = new ArrayList<>();
			while(results.next()) {
				warnings.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return warnings;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Fetches the warning in the current row of the set
	 * @param results the result set
	 * @return the warning in the current row
	 * @throws SQLException if one occurs
	 */
	protected Warning fetchFromSet(ResultSet results) throws SQLException {
		return new Warning(results.getInt("id"), results.getInt("warned_user_id"), 
				results.getInt("warning_user_id"), results.getString("violation"), 
				results.getString("action_taken"), results.getString("next_action"),
				results.getString("notes"), results.getTimestamp("created_at"),
				results.getTimestamp("updated_at"));
	}
	
	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE warnings ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "warned_user_id INT NOT NULL, "
				+ "warning_user_id INT NOT NULL, "
				+ "violation TEXT NOT NULL, "
				+ "action_taken TEXT NOT NULL, "
				+ "next_action TEXT, "
				+ "notes TEXT, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_warn_warned_user (warned_user_id), "
				+ "INDEX ind_warn_warning_user (warning_user_id), "
				+ "FOREIGN KEY (warned_user_id) REFERENCES users(id), "
				+ "FOREIGN KEY (warning_user_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
