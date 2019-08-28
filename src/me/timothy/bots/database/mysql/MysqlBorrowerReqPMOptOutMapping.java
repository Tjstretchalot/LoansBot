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
import me.timothy.bots.database.BorrowerReqPMOptOutMapping;
import me.timothy.bots.models.BorrowerReqPMOptOut;

public class MysqlBorrowerReqPMOptOutMapping extends MysqlObjectWithIDMapping<BorrowerReqPMOptOut> implements BorrowerReqPMOptOutMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlBorrowerReqPMOptOutMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "borrower_req_pm_opt_outs", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"));
	}

	@Override
	public void save(BorrowerReqPMOptOut a) throws IllegalArgumentException {
		if(a.createdAt != null)
			a.createdAt.setNanos(0);
		
		try {
			PreparedStatement statement;
			if(a.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (user_id, created_at) VALUES (?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET user_id=?, created_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, a.userId);
			statement.setTimestamp(counter++, a.createdAt);

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
	public boolean contains(int userId) {
		return fetchByAction("SELECT 1 FROM " + table + " WHERE user_id=? LIMIT 1",
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, userId)),
				new PreparedStatementFetchResult<Boolean>() {

					@Override
					public Boolean fetchResult(ResultSet set) throws SQLException {
						return set.first();
					}
		}).booleanValue();
	}

	@Override
	protected BorrowerReqPMOptOut fetchFromSet(ResultSet set) throws SQLException {
		return new BorrowerReqPMOptOut(set.getInt("id"), set.getInt("user_id"), set.getTimestamp("created_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		try(Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE " + table + " ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "user_id INT NOT NULL, "
					+ "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "PRIMARY KEY(id),"
					+ "UNIQUE KEY(user_id), "
					+ "FOREIGN KEY(user_id) REFERENCES users(id)"
					+ ")");
		}
	}
}
