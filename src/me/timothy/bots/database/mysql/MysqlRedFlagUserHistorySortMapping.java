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
import me.timothy.bots.database.MappingDatabase;
import me.timothy.bots.database.RedFlagUserHistoryCommentMapping;
import me.timothy.bots.database.RedFlagUserHistoryLinkMapping;
import me.timothy.bots.database.RedFlagUserHistorySortMapping;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.RedFlagUserHistorySort;
import me.timothy.bots.models.RedFlagUserHistorySort.RedFlagUserThing;

public class MysqlRedFlagUserHistorySortMapping extends MysqlObjectWithIDMapping<RedFlagUserHistorySort> implements RedFlagUserHistorySortMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlRedFlagUserHistorySortMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "redflag_uhist_sorts", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "report_id"),
				new MysqlColumn(Types.INTEGER, "sort"),
				new MysqlColumn(Types.TINYINT, "table_id"),
				new MysqlColumn(Types.INTEGER, "foreign_id"));
	}

	@Override
	public void save(RedFlagUserHistorySort a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		try {
			PreparedStatement statement;
			if(a.id < 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (report_id, sort, table_id, foreign_id) VALUES (?, ?, ?, ?)", 
						Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET report_id=?, sort=?, table_id=?, foreign_id=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, a.reportId);
			statement.setInt(counter++, a.sort);
			statement.setByte(counter++, (byte)a.table.value);
			statement.setInt(counter++, a.foreignId);

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
	public RedFlagUserHistorySort fetchNext(int reportId, int previous) {
		return fetchByAction("SELECT * FROM " + table + " WHERE report_id=? AND sort>?", 
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, reportId), 
						new MysqlTypeValueTuple(Types.INTEGER, previous)), 
				fetchFromSetFunction());
	}

	@Override
	public void deleteByReport(int reportId) {
		runStatement("DELETE FROM " + table + " WHERE report_id=?", new PreparedStatementSetVarsUnsafe(
				new MysqlTypeValueTuple(Types.INTEGER, reportId)));
	}
	
	@Override
	public void produceSort(MappingDatabase database, int reportId) {
		// this could be put into one sql statement if you're quite clever with joins but I couldn't
		// get it to work. this way is probably pretty inefficient but it at least it's pretty unlikely
		// to cause OutOfMemoryException's
		
		int sort = 1;
		
		RedFlagUserHistoryCommentMapping cMapping = database.getRedFlagUserHistoryCommentMapping();
		RedFlagUserHistoryLinkMapping lMapping = database.getRedFlagUserHistoryLinkMapping();
		
		RedFlagUserHistoryComment nextComment = cMapping.fetchNextAfter(reportId, new Timestamp(1000));
		RedFlagUserHistoryLink nextLink = lMapping.fetchNextAfter(reportId, new Timestamp(1000));
		
		while(nextComment != null || nextLink != null) {
			if(nextComment != null && (nextLink == null || nextLink.createdAt.after(nextComment.createdAt))) {
				List<RedFlagUserHistoryComment> allCommentsAtTimestamp = cMapping.fetchAtTimestamp(reportId, nextComment.createdAt);
				for(RedFlagUserHistoryComment comm : allCommentsAtTimestamp) {
					save(new RedFlagUserHistorySort(-1, reportId, sort++, RedFlagUserThing.Comment, comm.id));
				}
				
				nextComment = cMapping.fetchNextAfter(reportId, nextComment.createdAt);
			}else {
				List<RedFlagUserHistoryLink> allLinksAtTimestamp = lMapping.fetchAtTimestamp(reportId, nextLink.createdAt);
				for(RedFlagUserHistoryLink link : allLinksAtTimestamp) {
					save(new RedFlagUserHistorySort(-1, reportId, sort++, RedFlagUserThing.Link, link.id));
				}
				
				nextLink = lMapping.fetchNextAfter(reportId, nextLink.createdAt);
			}
		}
	}

	@Override
	protected RedFlagUserHistorySort fetchFromSet(ResultSet set) throws SQLException {
		return new RedFlagUserHistorySort(set.getInt("id"), set.getInt("report_id"), set.getInt("sort"), 
				RedFlagUserHistorySort.RedFlagUserThing.getByValue(set.getByte("table_id")), set.getInt("foreign_id"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "report_id INT NOT NULL, "
				+ "sort INT NOT NULL, "
				+ "table_id TINYINT NOT NULL, "
				+ "foreign_id INT NOT NULL, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_uhistsort_repid (report_id), "
				+ "FOREIGN KEY (report_id) REFERENCES red_flag_reports(id)"
				+ ")");
		statement.close();
	}

}
