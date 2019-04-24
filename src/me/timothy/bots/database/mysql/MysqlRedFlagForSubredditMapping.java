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
import me.timothy.bots.database.RedFlagForSubredditMapping;
import me.timothy.bots.models.RedFlagForSubreddit;

public class MysqlRedFlagForSubredditMapping extends MysqlObjectWithIDMapping<RedFlagForSubreddit> implements RedFlagForSubredditMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlRedFlagForSubredditMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "red_flag_subreddits", 
				new MysqlColumn(Types.INTEGER, "id", true), 
				new MysqlColumn(Types.VARCHAR, "subreddit"),
				new MysqlColumn(Types.LONGVARCHAR, "description"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"));
	}

	@Override
	public void save(RedFlagForSubreddit a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (subreddit, description, created_at) VALUES (?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET subreddit=?, description=?, created_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setString(counter++, a.subreddit);
			statement.setString(counter++, a.description);
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
	public RedFlagForSubreddit fetchBySubreddit(String subreddit) {
		return fetchByAction("SELECT * FROM " + table + " WHERE subreddit=?", 
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.VARCHAR, subreddit)),
				fetchFromSetFunction());
	}

	@Override
	protected RedFlagForSubreddit fetchFromSet(ResultSet set) throws SQLException {
		return new RedFlagForSubreddit(set.getInt("id"), set.getString("subreddit"), 
				set.getString("description"), set.getTimestamp("created_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id int NOT NULL AUTO_INCREMENT, "
				+ "subreddit varchar(64) NOT NULL, "
				+ "description text NOT NULL, "
				+ "created_at timestamp NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_rffs_subreddit (subreddit)"
				+ ")");
		statement.close();
	}
}
