package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.FailedLoginAttemptMapping;
import me.timothy.bots.models.FailedLoginAttempt;

public class MysqlFailedLoginAttemptMapping extends MysqlObjectWithIDMapping<FailedLoginAttempt> implements FailedLoginAttemptMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlFailedLoginAttemptMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "failed_login_attempts", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.VARCHAR, "username"),
				new MysqlColumn(Types.TIMESTAMP, "attempted_at"));
	}

	@Override
	public void save(FailedLoginAttempt a) throws IllegalArgumentException {
		if(a.attemptedAt != null) { a.attemptedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO failed_login_attempts (username, attempted_at) VALUES (?, ?)",
								Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE failed_login_attempts SET username=?, attempted_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setString(counter++, a.username);
			statement.setTimestamp(counter++, a.attemptedAt);
			

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
					throw new RuntimeException("Expected generated keys from " + table + " but didn't get any!");
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
	protected FailedLoginAttempt fetchFromSet(ResultSet set) throws SQLException {
		return new FailedLoginAttempt(set.getInt("id"), set.getString("username"), set.getTimestamp("attempted_at"));
	}

	@Override
	public void prune() {
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE attempted_at < DATE_SUB(NOW(), INTERVAL 7 DAY)")) {
			statement.execute();
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void createTable() throws SQLException {
		try(Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE " + table + " ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "username VARCHAR(63) NOT NULL, "
					+ "attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "PRIMARY KEY (id)"
					+ ")");
		}
	}

}
