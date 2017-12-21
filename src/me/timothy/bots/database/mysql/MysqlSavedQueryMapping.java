package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.SavedQueryMapping;
import me.timothy.bots.models.SavedQuery;

public class MysqlSavedQueryMapping extends MysqlObjectWithIDMapping<SavedQuery> implements SavedQueryMapping {

	public MysqlSavedQueryMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "saved_queries", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.VARCHAR, "name"),
				new MysqlColumn(Types.BIT, "shared"),
				new MysqlColumn(Types.BIT, "always_shared"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(SavedQuery a) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected SavedQuery fetchFromSet(ResultSet set) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "name VARCHAR(255) NOT NULL, "
				+ "shared TINYINT(1) NOT NULL, "
				+ "always_shared TINYINT(1) NOT NULL, "
				+ "created_at timestamp NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_sq_shared (shared)"
				+ ")");
		statement.close();
	}

}
