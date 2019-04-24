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
import me.timothy.bots.database.RecheckMapping;
import me.timothy.bots.models.Recheck;

public class MysqlRecheckMapping extends MysqlObjectMapping<Recheck> implements RecheckMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlRecheckMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "rechecks", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.VARCHAR, "fullname"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(Recheck a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE rechecks SET fullname=?, created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO rechecks (fullname, created_at, updated_at) VALUES (?, ?, ?)", 
						Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setString(counter++, a.fullname);
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
					throw new RuntimeException("expected rechecks to return generated keys, but didn't get any");
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
	public List<Recheck> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM rechecks");
			
			ResultSet results = statement.executeQuery();
			List<Recheck> rechecks = new ArrayList<>();
			while(results.next()) { 
				rechecks.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			
			return rechecks;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void delete(Recheck recheck) {
		if(recheck.id < 1) {
			throw new IllegalArgumentException("Recheck deletion is done by id, so " + recheck + " is not valid");
		}
		
		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM rechecks WHERE id=?");
			statement.setInt(1, recheck.id);
			statement.execute();
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Gets the recheck in the current row of the set 
	 * @param results the resultset
	 * @return the recheck in the current row
	 * @throws SQLException if one occurs
	 */
	protected Recheck fetchFromSet(ResultSet results) throws SQLException {
		return new Recheck(results.getInt("id"), results.getString("fullname"), 
				results.getTimestamp("created_at"), results.getTimestamp("updated_at"));
	}
	
	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE rechecks ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "fullname VARCHAR(255) NOT NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
				+ "PRIMARY KEY(id)"
				+ ")");
		statement.close();
		
	}

}
