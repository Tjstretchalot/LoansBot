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
import me.timothy.bots.database.RecentPostMapping;
import me.timothy.bots.models.RecentPost;

public class MysqlRecentPostMapping extends MysqlObjectMapping<RecentPost> implements RecentPostMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlRecentPostMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "recent_posts", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.VARCHAR, "author"),
				new MysqlColumn(Types.VARCHAR, "subreddit"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}


	@Override
	public void save(RecentPost a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE recent_posts SET author=?, subreddit=? created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO recent_posts (author, subreddit, created_at, updated_at) VALUES (?, ?, ?, ?)", 
						Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setString(counter++, a.author);
			statement.setString(counter++, a.subreddit);
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
					throw new RuntimeException("expected recent_posts to return generated keys, but didn't get any");
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
	public List<RecentPost> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM recent_posts");

			ResultSet results = statement.executeQuery();
			List<RecentPost> recentPosts = new ArrayList<>();
			while(results.next()) { 
				recentPosts.add(fetchFromSet(results));
			}
			results.close();

			statement.close();

			return recentPosts;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<RecentPost> fetchByUsername(String username) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM recent_posts WHERE author=?");
			statement.setString(1, username);
			
			ResultSet results = statement.executeQuery();
			List<RecentPost> recentPosts = new ArrayList<>();
			while(results.next()) { 
				recentPosts.add(fetchFromSet(results));
			}
			results.close();

			statement.close();

			return recentPosts;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Gets the recent post in the current row of the set 
	 * @param results the resultset
	 * @return the recent post in the current row
	 * @throws SQLException if one occurs
	 */
	protected RecentPost fetchFromSet(ResultSet results) throws SQLException {
		return new RecentPost(results.getInt("id"), results.getString("author"),
				results.getString("subreddit"), results.getTimestamp("created_at"), 
				results.getTimestamp("updated_at"));
	}

	@Override
	public void deleteOldEntries() {
		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM recent_posts WHERE created_at < (NOW() - INTERVAL 7 DAY)");
			
			statement.execute();

			statement.close();
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE recent_posts ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "author VARCHAR(255) NOT NULL, "
				+ "subreddit VARCHAR(255) NOT NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
				+ "PRIMARY KEY(id)"
				+ ")");
		statement.close();
	}

}
