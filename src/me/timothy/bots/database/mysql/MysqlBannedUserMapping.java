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
import me.timothy.bots.database.BannedUserMapping;
import me.timothy.bots.models.BannedUser;

public class MysqlBannedUserMapping extends MysqlObjectMapping<BannedUser> implements BannedUserMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlBannedUserMapping(LoansDatabase database, Connection connection) {
		super(database, connection,  "banned_users",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at")
				);
	}

	@Override
	public void save(BannedUser a) throws IllegalArgumentException {
		if(a == null || !a.isValid()) 
			throw new IllegalArgumentException(a + " is null or invalid");
		
		if(a.id != -1)
			throw new UnsupportedOperationException("Modifying banned users is not supported");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO " + table + " (user_id, created_at, updated_at) values (?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			
			int counter = 1;
			statement.setInt(counter++, a.userID);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.updatedAt);
			
			statement.executeUpdate();

			ResultSet keys = statement.getGeneratedKeys();
			if(keys.next()) {
				a.id = keys.getInt(1);
			}else {
				keys.close();
				statement.close();
				throw new RuntimeException("Expected generated keys from banned_users, but didn't get any!");
			}
			
			keys.close();
			statement.close();
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Fetches the banned user in the current row of the set
	 * @param set the set
	 * @return the banned user in the current row
	 * @throws SQLException if one occurs
	 */
	@Override
	protected BannedUser fetchFromSet(ResultSet set) throws SQLException {
		return new BannedUser(set.getInt("id"), set.getInt("user_id"), set.getTimestamp("created_at"), set.getTimestamp("updated_at"));
	}

	@Override
	public boolean containsUserID(int userID) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE user_id=?");
			
			int counter = 1;
			statement.setInt(counter++, userID);
			
			ResultSet results = statement.executeQuery();
			
			boolean result = results.next();
			
			results.close();
			statement.close();
			
			return result;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void removeByUserID(int userID) {
		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE user_id=?");
			
			int counter = 1;
			statement.setInt(counter++, userID);
			
			statement.executeUpdate();
			
			statement.close();
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "user_id INT NOT NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_bnuser_user_id (user_id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id) "
				+ ")");
		statement.close();
	}

}
