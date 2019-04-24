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
import me.timothy.bots.database.LCCMapping;
import me.timothy.bots.models.LendersCampContributor;

public class MysqlLCCMapping extends MysqlObjectMapping<LendersCampContributor> implements LCCMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlLCCMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "lenders_camp_contributors", 
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.BIT, "bot_added"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(LendersCampContributor a) throws IllegalArgumentException {
		if(!a.isValid()) {
			throw new IllegalArgumentException(a + " is not valid");
		}
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE lenders_camp_contributors SET "
						+ "user_id=?, bot_added=?, created_at=?, updated_at=? WHERE id=?");
			}else { 
				statement = connection.prepareStatement("INSERT INTO lenders_camp_contributors "
						+ "(user_id, bot_added, created_at, updated_at) VALUES (?, ?, ?, ?)", 
						Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.userId);
			statement.setBoolean(counter++, a.botAdded);
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
					throw new RuntimeException("Expected lenders_camp_contributors to return generated keys, but didn't get any");
				}
				keys.close();
			}
			
			statement.close();
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean contains(int userId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM lenders_camp_contributors WHERE user_id=?");
			statement.setInt(1, userId);
			
			ResultSet results = statement.executeQuery();
			boolean contains = results.next();
			results.close();
			
			statement.close();
			return contains;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<LendersCampContributor> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM lenders_camp_contributors");
			
			ResultSet results = statement.executeQuery();
			List<LendersCampContributor> lccs = new ArrayList<>();
			while(results.next()) { 
				lccs.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return lccs;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Fetches the lenders camp contributor in the current row of the
	 * set 
	 * @param results the set to get the lenders camp contributor from
	 * @return the lenders camp contributor in the current row of the set
	 * @throws SQLException if one occurs
	 */
	protected LendersCampContributor fetchFromSet(ResultSet results) throws SQLException {
		return new LendersCampContributor(results.getInt("id"), results.getInt("user_id"), 
				results.getBoolean("bot_added"), results.getTimestamp("created_at"), 
				results.getTimestamp("updated_at"));
	}
	
	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE lenders_camp_contributors ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "user_id INT NOT NULL, "
				+ "bot_added TINYINT(1) NOT NULL DEFAULT 0, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_lcc_user_id (user_id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
