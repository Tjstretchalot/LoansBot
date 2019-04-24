package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.RedFlagQueueSpotMapping;
import me.timothy.bots.models.RedFlagQueueSpot;

public class MysqlRedFlagQueueSpotMapping extends MysqlObjectWithIDMapping<RedFlagQueueSpot> implements RedFlagQueueSpotMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlRedFlagQueueSpotMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "red_flag_queue_spots",
					new MysqlColumn(Types.INTEGER, "id", true),
					new MysqlColumn(Types.INTEGER, "report_id"),
					new MysqlColumn(Types.INTEGER, "username_id"),
					new MysqlColumn(Types.TIMESTAMP, "created_at"),
					new MysqlColumn(Types.TIMESTAMP, "started_at"),
					new MysqlColumn(Types.TIMESTAMP, "completed_at")
				);
	}

	@Override
	public void save(RedFlagQueueSpot a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid!");
		
		if (a.createdAt != null) { a.createdAt.setNanos(0); }
		if (a.startedAt != null) { a.startedAt.setNanos(0); }
		if (a.completedAt != null) { a.completedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if (a.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (report_id, username_id, "
						+ "created_at, started_at, completed_at) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET report_id=?, username_id=?, "
						+ "created_at=?, started_at=?, completed_at=? WHERE id=?");
			}
			
			int counter = 1;
			if(a.reportId != null)
				statement.setInt(counter++, a.reportId.intValue());
			else
				statement.setNull(counter++, Types.INTEGER);
			
			statement.setInt(counter++, a.usernameId);
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
					throw new RuntimeException("Expected generated keys from " + table + ", but didn't get any!");
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
	public RedFlagQueueSpot fetchOldestUncompleted() {
		return fetchByAction("SELECT * FROM " + table + " WHERE completed_at IS NULL ORDER BY created_at ASC LIMIT 1", 
				new PreparedStatementSetVarsUnsafe(), 
				fetchFromSetFunction());
	}

	@Override
	public List<RedFlagQueueSpot> fetchByReportId(int reportId, boolean onlyUncompleted) {
		return fetchByAction("SELECT * FROM " + table + " WHERE report_id=? AND (? = 0 OR completed_at IS NULL)", 
				new PreparedStatementSetVarsUnsafe(
						new MysqlTypeValueTuple(Types.INTEGER, reportId), 
						new MysqlTypeValueTuple(Types.BIT, onlyUncompleted)), 
				fetchListFromSetFunction());
	}

	@Override
	public List<RedFlagQueueSpot> fetchByUsername(int usernameId, boolean onlyUncompleted) {
		return fetchByAction("SELECT * FROM " + table + " WHERE username_id=? AND (? = 0 OR completed_at IS NULL)", 
				new PreparedStatementSetVarsUnsafe(
						new MysqlTypeValueTuple(Types.INTEGER, usernameId), 
						new MysqlTypeValueTuple(Types.BIT, onlyUncompleted ? 1 : 0)), 
				fetchListFromSetFunction());
	}

	@Override
	protected RedFlagQueueSpot fetchFromSet(ResultSet set) throws SQLException {
		int id = set.getInt("id");
		Integer reportId = set.getInt("report_id");
		if(set.wasNull())
			reportId = null;
		int usernameId = set.getInt("username_id");
		Timestamp createdAt = set.getTimestamp("created_at");
		Timestamp startedAt = set.getTimestamp("started_at");
		Timestamp completedAt = set.getTimestamp("completed_at");
		
		return new RedFlagQueueSpot(id, reportId, usernameId,  
				createdAt, startedAt, completedAt);
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id int NOT NULL AUTO_INCREMENT, "
				+ "report_id int NULL, "
				+ "username_id int NOT NULL, "
				+ "created_at timestamp NOT NULL DEFAULT '1000-01-01 00:00:00', "
				+ "started_at timestamp NULL DEFAULT NULL, "
				+ "completed_at timestamp NULL DEFAULT NULL, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_rfqs_report_id (report_id), "
				+ "INDEX ind_rfqs_username_id (username_id), "
				+ "FOREIGN KEY (report_id) REFERENCES red_flag_reports(id), "
				+ "FOREIGN KEY (username_id) REFERENCES usernames(id)"
				+ ")");
		statement.close();
	}

}
