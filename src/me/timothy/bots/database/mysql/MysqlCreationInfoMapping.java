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
import me.timothy.bots.database.CreationInfoMapping;
import me.timothy.bots.models.CreationInfo;

public class MysqlCreationInfoMapping extends MysqlObjectMapping<CreationInfo> implements CreationInfoMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlCreationInfoMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "creation_infos",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "loan_id"),
				new MysqlColumn(Types.INTEGER, "type"),
				new MysqlColumn(Types.LONGVARCHAR, "thread"),
				new MysqlColumn(Types.LONGVARCHAR, "reason"),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at")
				);
	}

	@Override
	public void save(CreationInfo a) throws IllegalArgumentException {
		if(!a.isValid()) {
			throw new IllegalArgumentException(a + " is not valid");
		}
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE creation_infos SET loan_id=?, type=?, thread=?, "
						+ "reason=?, user_id=?, created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO creation_infos (loan_id, type, thread, "
						+ "reason, user_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.loanId);
			statement.setInt(counter++, a.type.getTypeNum());
			statement.setString(counter++, a.thread);
			statement.setString(counter++, a.reason);
			if(a.userId > 0) {
				statement.setInt(counter++, a.userId);
			}else {
				statement.setNull(counter++, a.userId);
			}
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
					throw new RuntimeException("Expected generated keys for creation_infos, but didn't get any!");
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
	public CreationInfo fetchById(int id) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM creation_infos WHERE id=?");
			statement.setInt(1, id);
			
			ResultSet results = statement.executeQuery();
			CreationInfo cInfo = null;
			if(results.next()) {
				cInfo = fetchFromSet(results);
			}
			results.close();
			
			statement.close();
			return cInfo;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public CreationInfo fetchByLoanId(int loanId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM creation_infos WHERE loan_id=?");
			statement.setInt(1, loanId);
			
			ResultSet results = statement.executeQuery();
			CreationInfo cInfo = null;
			if(results.next()) {
				cInfo = fetchFromSet(results);
			}
			results.close();
			
			statement.close();
			return cInfo;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<CreationInfo> fetchManyByLoanIds(int... loanIds) {
		String where = " WHERE ";
		boolean first = true;
		
		for(int i = 0; i < loanIds.length; i++) {
			if(!first) {
				where += " OR ";
			}else {
				first = false;
			}
			
			where += "loan_id=?";
		}
		
		
		
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM creation_infos" + (loanIds.length > 0 ? where : " WHERE 0"));
			int counter = 1;
			for(int loanId : loanIds) {
				statement.setInt(counter++, loanId);
			}
			
			ResultSet results = statement.executeQuery();
			List<CreationInfo> cInfos = new ArrayList<>();
			while(results.next()) {
				cInfos.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return cInfos;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<CreationInfo> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM creation_infos");
			
			ResultSet results = statement.executeQuery();
			List<CreationInfo> creationInfos = new ArrayList<>();
			while(results.next()) {
				creationInfos.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return creationInfos;
		}catch(SQLException ex) { 
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Fetches the CreationInfo in the current row in the set. If the user_id 
	 * column is null, it is set to -1.
	 * 
	 * @param results the result set
	 * @return the creation info in the current row
	 * @throws SQLException if one occurs
	 */
	protected CreationInfo fetchFromSet(ResultSet results) throws SQLException {
		CreationInfo cInfo = new CreationInfo();
		cInfo.id = results.getInt("id");
		cInfo.loanId = results.getInt("loan_id");
		cInfo.type = CreationInfo.CreationType.getByTypeNum(results.getInt("type"));
		cInfo.thread = results.getString("thread");
		cInfo.reason = results.getString("reason");
		cInfo.userId = results.getInt("user_id");
		if(results.wasNull()) { cInfo.userId = -1; }
		cInfo.createdAt = results.getTimestamp("created_at");
		cInfo.updatedAt = results.getTimestamp("updated_at");
		return cInfo;
	}
	
	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE creation_infos ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "loan_id INT NOT NULL, "
				+ "type INT NOT NULL, "
				+ "thread TEXT, "
				+ "reason TEXT, "
				+ "user_id INT, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_cinfoloan_id (loan_id), "
				+ "INDEX ind_cinfouser_id (user_id), "
				+ "FOREIGN KEY (loan_id) REFERENCES loans(id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
