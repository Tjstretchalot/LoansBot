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
import me.timothy.bots.database.RedFlagUserHistoryLinkMapping;
import me.timothy.bots.models.RedFlagUserHistoryLink;

public class MysqlRedFlagUserHistoryLinkMapping extends MysqlObjectWithIDMapping<RedFlagUserHistoryLink> implements RedFlagUserHistoryLinkMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlRedFlagUserHistoryLinkMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "redflag_uhist_links",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "report_id"),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.VARCHAR, "fullname"),
				new MysqlColumn(Types.LONGVARCHAR, "title"),
				new MysqlColumn(Types.LONGVARCHAR, "url"),
				new MysqlColumn(Types.LONGVARCHAR, "self_text"),
				new MysqlColumn(Types.LONGVARCHAR, "permalink"),
				new MysqlColumn(Types.VARCHAR, "subreddit"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "edited_at"));
	}

	@Override
	public void save(RedFlagUserHistoryLink a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid!");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.editedAt != null) { a.editedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id < 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + "(report_id, user_id, fullname, title, url, self_text, permalink, "
						+ "subreddit, created_at, edited_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET report_id=?, user_id=?, fullname=?, title=?, url=?, self_text=?, "
						+ "permalink=?, subreddit=?, created_at=?, edited_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, a.reportId);
			statement.setInt(counter++, a.userId);
			statement.setString(counter++, a.fullname);
			statement.setString(counter++, a.title);
			statement.setString(counter++, a.url);
			statement.setString(counter++, a.selfText);
			statement.setString(counter++, a.permalink);
			statement.setString(counter++, a.subreddit);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.editedAt);

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
	public void deleteByReportID(int reportId) {
		runStatement("DELETE FROM " + table + " WHERE report_id=?", new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, reportId)));
	}

	@Override
	public List<RedFlagUserHistoryLink> fetchAtTimestamp(int reportId, Timestamp timestamp) {
		return fetchByAction("SELECT * FROM " + table + " WHERE report_id=? AND created_at=?", 
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, reportId), 
						new MysqlTypeValueTuple(Types.TIMESTAMP, timestamp)), 
				fetchListFromSetFunction());
	}

	@Override
	public RedFlagUserHistoryLink fetchNextAfter(int reportId, Timestamp timestamp) {
		return fetchByAction("SELECT * FROM " + table + " WHERE report_id=? AND created_at>? ORDER BY created_at ASC LIMIT 1", 
				new PreparedStatementSetVarsUnsafe(
						new MysqlTypeValueTuple(Types.INTEGER, reportId),
						new MysqlTypeValueTuple(Types.TIMESTAMP, timestamp)),
				fetchFromSetFunction());
	}


	@Override
	protected RedFlagUserHistoryLink fetchFromSet(ResultSet set) throws SQLException {
		return new RedFlagUserHistoryLink(
				set.getInt("id"), set.getInt("report_id"), set.getInt("user_id"), set.getString("fullname"),
				set.getString("title"), set.getString("url"), set.getString("self_text"), set.getString("permalink"),
				set.getString("subreddit"), set.getTimestamp("created_at"), set.getTimestamp("edited_at")
				);
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "report_id INT NOT NULL, "
				+ "user_id INT NOT NULL, "
				+ "fullname VARCHAR(63) NOT NULL, "
				+ "title TEXT NOT NULL, "
				+ "url TEXT NULL, "
				+ "self_text TEXT NULL, "
				+ "permalink TEXT NOT NULL, "
				+ "subreddit VARCHAR(63) NOT NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+ "edited_at TIMESTAMP NULL DEFAULT NULL, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_rfuhislink_repid (report_id), "
				+ "INDEX ind_rfuhislink_usrid (user_id), "
				+ "FOREIGN KEY (report_id) REFERENCES red_flag_reports(id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")"
				);
		statement.close();
	}

}
