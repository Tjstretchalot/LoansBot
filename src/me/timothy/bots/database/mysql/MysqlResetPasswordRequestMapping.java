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
import me.timothy.bots.database.ResetPasswordRequestMapping;
import me.timothy.bots.models.ResetPasswordRequest;

public class MysqlResetPasswordRequestMapping extends MysqlObjectMapping<ResetPasswordRequest> implements ResetPasswordRequestMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlResetPasswordRequestMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "reset_password_requests",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.VARCHAR, "reset_code"),
				new MysqlColumn(Types.BIT, "reset_code_sent"),
				new MysqlColumn(Types.BIT, "reset_code_used"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(ResetPasswordRequest a) throws IllegalArgumentException {
		if(!a.isValid()) 
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE reset_password_requests SET "
						+ "user_id=?, reset_code=?, reset_code_sent=?, reset_code_used=?, created_at=?, "
						+ "updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO reset_password_requests "
						+ "(user_id, reset_code, reset_code_sent, reset_code_used, created_at, "
						+ "updated_at) VALUES (?, ?, ?, ?, ? ,?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.userId);
			statement.setString(counter++, a.resetCode);
			statement.setBoolean(counter++, a.resetCodeSent);
			statement.setBoolean(counter++, a.resetCodeUsed);
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
					throw new RuntimeException("expected reset_password_requests to generate keys but it didn't!");
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
	public List<ResetPasswordRequest> fetchUnsent() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM reset_password_requests WHERE reset_code_sent=0");
			
			ResultSet results = statement.executeQuery();
			List<ResetPasswordRequest> rprs = new ArrayList<>();
			while(results.next()) {
				rprs.add(fetchFromSet(results));
			}
			results.close();
			
			return rprs;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ResetPasswordRequest> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM reset_password_requests");
			
			ResultSet results = statement.executeQuery();
			List<ResetPasswordRequest> rprs = new ArrayList<>();
			while(results.next()) {
				rprs.add(fetchFromSet(results));
			}
			results.close();
			
			return rprs;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Fetches the reset password request in the current row of the result set
	 * @param results the result set
	 * @return the reset password request in the current row
	 * @throws SQLException if one occurs
	 */
	protected ResetPasswordRequest fetchFromSet(ResultSet results) throws SQLException {
		return new ResetPasswordRequest(results.getInt("id"), results.getInt("user_id"), 
				results.getString("reset_code"), results.getBoolean("reset_code_sent"), 
				results.getBoolean("reset_code_used"), results.getTimestamp("created_at"), 
				results.getTimestamp("updated_at"));
	}
	
	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE reset_password_requests ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "user_id INT NOT NULL, "
				+ "reset_code VARCHAR(255), "
				+ "reset_code_sent TINYINT(1) NOT NULL DEFAULT 0, "
				+ "reset_code_used TINYINT(1) NOT NULL DEFAULT 0, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_rpr_user_id (user_id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")");
	}

}
