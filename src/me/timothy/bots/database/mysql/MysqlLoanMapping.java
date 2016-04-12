package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.LoanMapping;
import me.timothy.bots.models.Loan;

public class MysqlLoanMapping extends MysqlObjectMapping<Loan> implements LoanMapping {
	private static Logger logger = LogManager.getLogger();
	
	public MysqlLoanMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "loans",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "lender_id"),
				new MysqlColumn(Types.INTEGER, "borrower_id"),
				new MysqlColumn(Types.INTEGER, "principal_cents"),
				new MysqlColumn(Types.INTEGER, "principal_repayment_cents"),
				new MysqlColumn(Types.BIT, "unpaid"),
				new MysqlColumn(Types.BIT, "deleted"),
				new MysqlColumn(Types.LONGVARCHAR, "deleted_reason"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at"), 
				new MysqlColumn(Types.TIMESTAMP, "deleted_at"));
	}

	@Override
	public void save(Loan a) throws IllegalArgumentException {
		if(!a.isValid())
			throw new IllegalArgumentException(a + " is not valid");
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		if(a.deletedAt != null) { a.deletedAt.setNanos(0); }
		
		try {
			PreparedStatement statement;
			
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE loans SET lender_id=?, borrower_id=?, "
						+ "principal_cents=?, principal_repayment_cents=?, unpaid=?, deleted=?, deleted_reason=?, "
						+ "created_at=?, updated_at=?, deleted_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO loans (lender_id, borrower_id, "
						+ "principal_cents, principal_repayment_cents, unpaid, deleted, deleted_reason, created_at, "
						+ "updated_at, deleted_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.lenderId);
			statement.setInt(counter++, a.borrowerId);
			statement.setInt(counter++, a.principalCents);
			statement.setInt(counter++, a.principalRepaymentCents);
			statement.setBoolean(counter++, a.unpaid);
			statement.setBoolean(counter++, a.deleted);
			statement.setString(counter++, a.deletedReason);
			statement.setTimestamp(counter++, a.createdAt);
			statement.setTimestamp(counter++, a.updatedAt);
			statement.setTimestamp(counter++, a.deletedAt);
			
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
					throw new RuntimeException("Expected generated keys for loans table, but didn't get any!");
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
	public List<Integer> fetchLenderIdsWithNewLoanSince(Timestamp timestamp) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT lender_id FROM loans WHERE created_at>?");
			statement.setTimestamp(1, timestamp);
			
			ResultSet results = statement.executeQuery();
			List<Integer> lenderIds = new ArrayList<>();
			while(results.next()) {
				lenderIds.add(results.getInt(1));
			}
			results.close();
			
			statement.close();
			return lenderIds;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<Loan> fetchWithBorrowerAndOrLender(int borrowerId, int lenderId, boolean strict) {
		final String strictOperatorStr = strict ? "AND" : "OR";
		
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM loans WHERE lender_id=? " + strictOperatorStr 
					+ " borrower_id=?");
			
			int counter = 1;
			statement.setInt(counter++, lenderId);
			statement.setInt(counter++, borrowerId);
			
			ResultSet results = statement.executeQuery();
			List<Loan> loans = new ArrayList<>();
			while(results.next()) {
				loans.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return loans;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public int fetchNumberOfLoansWithUserAsLender(int lenderId) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM loans WHERE lender_id=? AND deleted=0");
			statement.setInt(1, lenderId);
			
			ResultSet results = statement.executeQuery();
			int numLoans = 0;
			if(!results.next()) {
				results.close();
				statement.close();
				throw new RuntimeException("COUNT(*) should always return 1 row, but it didn't");
			}
			numLoans = results.getInt(1);
			results.close();
			
			statement.close();
			return numLoans;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<Loan> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM loans");
			
			ResultSet results = statement.executeQuery();
			List<Loan> loans = new ArrayList<>();
			while(results.next()) {
				loans.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return loans;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Fetches the loan in the current row of the result set
	 * @param results the result set
	 * @return the loan in the current row
	 * @throws SQLException if one occurs
	 */
	protected Loan fetchFromSet(ResultSet results) throws SQLException {
		return new Loan(results.getInt("id"), results.getInt("lender_id"), results.getInt("borrower_id"), 
				results.getInt("principal_cents"), results.getInt("principal_repayment_cents"), 
				results.getBoolean("unpaid"), results.getBoolean("deleted"), results.getString("deleted_reason"), 
				results.getTimestamp("created_at"), results.getTimestamp("updated_at"), results.getTimestamp("deleted_at"));
	}

	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE loans ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "lender_id INT NOT NULL, "
				+ "borrower_id INT NOT NULL, "
				+ "principal_cents INT NOT NULL, "
				+ "principal_repayment_cents INT NOT NULL DEFAULT 0, "
				+ "unpaid TINYINT(1) NOT NULL DEFAULT 0, "
				+ "deleted TINYINT(1) NOT NULL DEFAULT 0, "
				+ "deleted_reason TEXT, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "deleted_at TIMESTAMP NULL DEFAULT NULL, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_loan_lender_id (lender_id), "
				+ "INDEX ind_loan_borrower_id (borrower_id), "
				+ "FOREIGN KEY (lender_id) REFERENCES users(id), "
				+ "FOREIGN KEY (borrower_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
