package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.DelayedVettingRequestMapping;
import me.timothy.bots.models.DelayedVettingRequest;

public class MysqlDelayedVettingRequestMapping extends MysqlObjectWithIDMapping<DelayedVettingRequest> implements DelayedVettingRequestMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlDelayedVettingRequestMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "delayed_vetting_requests",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.INTEGER, "number_loans"),
				new MysqlColumn(Types.LONGVARCHAR, "reason"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "rerequested_at"));
	}

	@Override
	public void save(DelayedVettingRequest a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.rerequestedAt != null) { a.rerequestedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (user_id, number_loans, reason, created_at, rerequested_at) VALUES"
						+ "(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET user_id=?, number_loans=?, reason=?, created_at=?, rerequested_at=? "
						+ "WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, a.userId);
			statement.setInt(counter++, a.numberLoans);
			statement.setString(counter++, a.reason);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.rerequestedAt);

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
	public DelayedVettingRequest fetchByUserId(int userId) {
		return fetchByAction("SELECT * FROM " + table + " WHERE user_id=? AND rerequested_at IS NULL LIMIT 1", 
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, userId)),
				fetchFromSetFunction());
	}

	@Override
	protected DelayedVettingRequest fetchFromSet(ResultSet set) throws SQLException {
		return new DelayedVettingRequest(set.getInt("id"), set.getInt("user_id"), set.getInt("number_loans"), 
				set.getString("reason"), set.getTimestamp("created_at"), set.getTimestamp("rerequested_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		try(Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE " + table + " ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "user_id INT NOT NULL, "
					+ "number_loans INT NOT NULL, "
					+ "reason TEXT NOT NULL, "
					+ "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
					+ "rerequested_at TIMESTAMP NULL DEFAULT NULL, "
					+ "PRIMARY KEY(id), "
					+ "INDEX ind_dvr_user_id (user_id), "
					+ "FOREIGN KEY (user_id) REFERENCES users(id)"
					+ ")");
		}
	}

}
