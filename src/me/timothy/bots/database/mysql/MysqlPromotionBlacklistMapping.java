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
import me.timothy.bots.database.PromotionBlacklistMapping;
import me.timothy.bots.models.PromotionBlacklist;

public class MysqlPromotionBlacklistMapping extends MysqlObjectWithIDMapping<PromotionBlacklist> implements PromotionBlacklistMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlPromotionBlacklistMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "promo_blacklist_users",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.INTEGER, "mod_user_id"),
				new MysqlColumn(Types.LONGVARCHAR, "reason"),
				new MysqlColumn(Types.TIMESTAMP, "added_at"),
				new MysqlColumn(Types.TIMESTAMP, "removed_at"));
	}

	@Override
	public void save(PromotionBlacklist a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid!");
		
		if(a.addedAt != null) { a.addedAt.setNanos(0); }
		if(a.removedAt != null) { a.removedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			
			if(a.id < 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (user_id, mod_user_id, reason, added_at, removed_at) VALUES (?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET user_id=?, mod_user_id=?, reason=?, added_at=?, removed_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, a.userId);
			statement.setInt(counter++, a.modUserId);
			statement.setString(counter++, a.reason);
			statement.setTimestamp(counter++, a.addedAt);
			statement.setTimestamp(counter++, a.removedAt);
			
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
	public boolean contains(int personId) {
		return fetchByAction("SELECT 1 FROM " + table + " WHERE user_id=? AND removed_at IS NULL LIMIT 1",
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, personId)),
				(set) -> set.first());
	}

	@Override
	protected PromotionBlacklist fetchFromSet(ResultSet set) throws SQLException {
		return new PromotionBlacklist(set.getInt("id"), set.getInt("user_id"), set.getInt("mod_user_id"), 
				set.getString("reason"), set.getTimestamp("added_at"), set.getTimestamp("removed_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "user_id INT NOT NULL, "
				+ "mod_user_id INT NOT NULL, "
				+ "reason TEXT NOT NULL, "
				+ "added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+ "removed_at TIMESTAMP NULL DEFAULT NULL, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_promoblist_userid (user_id), "
				+ "INDEX ind_promoblist_museid (mod_user_id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id), "
				+ "FOREIGN KEY (mod_user_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
