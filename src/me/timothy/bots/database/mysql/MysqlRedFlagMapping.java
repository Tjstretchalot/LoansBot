package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.RedFlagMapping;
import me.timothy.bots.models.RedFlag;

public class MysqlRedFlagMapping extends MysqlObjectWithIDMapping<RedFlag> implements RedFlagMapping {
	private static final Logger logger = LogManager.getLogger();

	public MysqlRedFlagMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "red_flags", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "report_id"), 
				new MysqlColumn(Types.VARCHAR, "type"),
				new MysqlColumn(Types.VARCHAR, "identifier"),
				new MysqlColumn(Types.LONGVARCHAR, "description"),
				new MysqlColumn(Types.INTEGER, "count"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"));
	}

	@Override
	public void save(RedFlag a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if (a.createdAt != null) { a.createdAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id <= 0) {
				statement = connection.prepareStatement("INSERT INTO " + table + " (report_id, type, identifier, description, count, "
						+ "created_at) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else {
				statement = connection.prepareStatement("UPDATE " + table + " SET report_id=?, type=?, identifier=?, description=?, count=?, created_at=? WHERE id=?");
			}
			
			int counter = 1;
			statement.setInt(counter++, a.reportId);
			statement.setString(counter++, a.type.databaseIdentifier);
			statement.setString(counter++, a.identifier);
			statement.setString(counter++, a.description);
			statement.setInt(counter++, a.count);
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
	public List<RedFlag> fetchByReportID(int reportId) {
		return fetchByAction("SELECT * FROM " + table + " WHERE report_id=?", 
				new PreparedStatementSetVarsUnsafe(new MysqlTypeValueTuple(Types.INTEGER, reportId)), 
				fetchListFromSetFunction());
	}

	@Override
	public RedFlag fetchByReportAndTypeAndIden(int reportId, String type, String iden) {
		return fetchByAction("SELECT * FROM " + table + " WHERE report_id=? AND type=? AND identifier=?", 
				new PreparedStatementSetVarsUnsafe(
						new MysqlTypeValueTuple(Types.INTEGER, reportId),
						new MysqlTypeValueTuple(Types.VARCHAR, type), 
						new MysqlTypeValueTuple(Types.VARCHAR, iden)), 
				fetchFromSetFunction());
	}

	@Override
	protected RedFlag fetchFromSet(ResultSet set) throws SQLException {
		return new RedFlag(set.getInt("id"), set.getInt("report_id"), set.getString("type"), set.getString("identifier"), set.getString("description"), 
				set.getInt("count"), set.getTimestamp("created_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id int NOT NULL AUTO_INCREMENT, "
				+ "report_id int NOT NULL, "
				+ "type VARCHAR(50) NOT NULL, "
				+ "identifier VARCHAR(255) NOT NULL, "
				+ "description TEXT NOT NULL, "
				+ "count INT NOT NULL, "
				+ "created_at timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_rf_uniq (report_id, type, identifier), " // this can be used for foreign key as well
				+ "FOREIGN KEY (report_id) REFERENCES red_flag_reports(id)"
				+ ")");
		statement.close();
	}

}
