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
import me.timothy.bots.database.RepaymentMapping;
import me.timothy.bots.models.Repayment;

public class MysqlRepaymentMapping extends MysqlObjectMapping<Repayment> implements RepaymentMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlRepaymentMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "repayments",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "loan_id"),
				new MysqlColumn(Types.INTEGER, "amount_cents"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"));
	}

	@Override
	public void save(Repayment a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE repayments SET loan_id=?, amount_cents=?, "
						+ "created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO repayments (loan_id, amount_cents, "
						+ "created_at, updated_at) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.loanId);	
			statement.setInt(counter++, a.amountCents);
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
					throw new RuntimeException("Expected generated keys from repayments but didn't get any!");
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
	public List<Repayment> fetchByLoanId(int loanId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM repayments WHERE id=?");
			statement.setInt(1, loanId);
			
			ResultSet results = statement.executeQuery();
			List<Repayment> repayments = new ArrayList<>();
			while(results.next()) {
				repayments.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return repayments;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Repayment> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM repayments");
			
			ResultSet results = statement.executeQuery();
			List<Repayment> repayments = new ArrayList<>();
			while(results.next()) {
				repayments.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return repayments;
		}catch(SQLException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Fetches the repayment in the current row of the result set
	 * @param results the result set
	 * @return the repayment in the current row
	 * @throws SQLException if one occurs
	 */
	protected Repayment fetchFromSet(ResultSet results) throws SQLException {
		return new Repayment(results.getInt("id"), results.getInt("loan_id"), 
				results.getInt("amount_cents"), results.getTimestamp("created_at"), 
				results.getTimestamp("updated_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE repayments ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "loan_id INT NOT NULL, "
				+ "amount_cents INT NOT NULL, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1000-01-01 00:00:00', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY(id), "
				+ "INDEX ind_repay_loan_id (loan_id), "
				+ "FOREIGN KEY (loan_id) REFERENCES loans(id)"
				+ ")");
		statement.close();
	}

}
