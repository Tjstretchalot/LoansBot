package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.FullnameMapping;
import me.timothy.bots.models.Fullname;

public class MysqlFullnameMapping extends MysqlObjectMapping<Fullname> implements FullnameMapping {
	private static Logger logger = LogManager.getLogger();
	public MysqlFullnameMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Fullname fullname) throws IllegalArgumentException {
		try {
			PreparedStatement statement;
			if(fullname.id > 0) {
				statement = connection.prepareStatement("UPDATE fullnames SET fullname=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO fullnames (fullname) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setString(counter++, fullname.fullname);
			
			if(fullname.id > 0) {
				statement.setInt(counter++, fullname.id);
				statement.execute();
			}else {
				statement.execute();
				
				ResultSet keys = statement.getGeneratedKeys();
				if(keys.next()) {
					fullname.id = keys.getInt(1);
				}else {
					throw new IllegalStateException("no generated keys");
				}
				keys.close();
			}
			statement.close();
			
		}catch(SQLException sqlE) {
			logger.throwing(sqlE);
			throw new RuntimeException(sqlE);
		}
	}

	@Override
	public boolean contains(String fullname) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM fullnames WHERE fullname=?");
			statement.setString(1, fullname);
			
			ResultSet results = statement.executeQuery();
			boolean contains = results.next();
			results.close();
			
			statement.close();
			
			return contains;
		}catch(SQLException sqlE) {
			logger.throwing(sqlE);
			throw new RuntimeException(sqlE);
		}
	}

	@Override
	public List<Fullname> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM fullnames");
			
			List<Fullname> fullnames = new ArrayList<>();
			ResultSet results = statement.executeQuery();
			while(results.next()) {
				fullnames.add(fetchFromSet(results));
			}
			results.close();
			statement.close();
			
			return fullnames;
		} catch (SQLException sqlE) {
			logger.throwing(sqlE);
			throw new RuntimeException(sqlE);
		}
	}

	/**
	 * Fetches a fullname from the result set
	 * 
	 * @param set the result set to fetch from
	 * @return the fullname in the current row of the set
	 * @throws SQLException if one occurs
	 */
	protected Fullname fetchFromSet(ResultSet set) throws SQLException {
		return new Fullname(set.getInt("id"), set.getString("fullname"));
	}
}
