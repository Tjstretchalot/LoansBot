package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.RedFlagReportMapping;
import me.timothy.bots.models.RedFlagReport;

public class MysqlRedFlagReportMapping extends MysqlObjectWithIDMapping<RedFlagReport> implements RedFlagReportMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlRedFlagReportMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "red_flag_reports",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "username_id"),
				new MysqlColumn(Types.VARCHAR, "after_fullname"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "started_at"),
				new MysqlColumn(Types.TIMESTAMP, "completed_at")
				);
	}

	@Override
	public void save(RedFlagReport a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if (a.createdAt != null) { a.createdAt.setNanos(0); }
		if (a.startedAt != null) { a.startedAt.setNanos(0); }
		if (a.completedAt != null) { a.completedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (username_id, after_fullname, created_at, "
						+ "started_at, completed_at) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET username_id=?, after_fullname=?, created_at=?, "
						+ "started_at=?, completed_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, a.usernameId);
			statement.setString(counter++, a.afterFullname);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.startedAt);
			statement.setTimestamp(counter++, a.completedAt);
			
			if (a.id > 0) {
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
					throw new RuntimeException("Expected generated keys for " + table + ", but didn't get any!");
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
	public List<RedFlagReport> fetchByUsernameID(int usernameId) {
		return fetchByAction("SELECT * FROM " + table + " WHERE username_id=?", 
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, usernameId)),
				fetchListFromSetFunction());
	}

	@Override
	protected RedFlagReport fetchFromSet(ResultSet set) throws SQLException {
		return new RedFlagReport(set.getInt("id"), set.getInt("username_id"), set.getString("after_fullname"), 
				set.getTimestamp("created_at"), set.getTimestamp("started_at"), set.getTimestamp("completed_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "username_id INT NOT NULL, "
				+ "after_fullname VARCHAR(50) NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1000-01-01 00:00:00', "
				+ "started_at TIMESTAMP NULL DEFAULT NULL, "
				+ "completed_at TIMESTAMP NULL DEFAULT NULL, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_rfr_username_id (username_id), "
				+ "FOREIGN KEY (username_id) REFERENCES usernames(id)"
				+ ")");
		statement.close();
	}

}
