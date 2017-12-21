package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.SavedQueryUserMapping;
import me.timothy.bots.models.SavedQueryUser;

public class MysqlSavedQueryUserMapping extends MysqlObjectWithIDMapping<SavedQueryUser> implements SavedQueryUserMapping {

	public MysqlSavedQueryUserMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "saved_query_users",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "saved_query_id"),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.BIT, "owned"),
				new MysqlColumn(Types.BIT, "inverse"));
	}

	@Override
	public void save(SavedQueryUser a) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected SavedQueryUser fetchFromSet(ResultSet set) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void createTable() throws SQLException {

		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE " + table + " ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "saved_query_id INT NOT NULL, "
				+ "user_id INT NOT NULL, "
				+ "owned TINYINT(1) NOT NULL, "
				+ "inverse TINYINT(1) NOT NULL, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_squ_sqid (saved_query_id), "
				+ "INDEX ind_squ_uid (user_id), "
				+ "FOREIGN KEY (saved_query_id) REFERENCES saved_queries(id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
