package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.SavedQueryParamMapping;
import me.timothy.bots.models.SavedQueryParam;

public class MysqlSavedQueryParamMapping extends MysqlObjectWithIDMapping<SavedQueryParam> implements SavedQueryParamMapping {

	public MysqlSavedQueryParamMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "saved_query_params", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "saved_query_id"),
				new MysqlColumn(Types.VARCHAR, "name"),
				new MysqlColumn(Types.LONGVARCHAR, "options")
				);
	}

	@Override
	public void save(SavedQueryParam a) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected SavedQueryParam fetchFromSet(ResultSet set) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "saved_query_id INT NOT NULL, "
				+ "name VARCHAR(255) NOT NULL, "
				+ "options TEXT NOT NULL, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_savquepar_sqid (saved_query_id), "
				+ "FOREIGN KEY (saved_query_id) REFERENCES saved_queries(id) "
				+ ")");
		statement.close();
	}

}
