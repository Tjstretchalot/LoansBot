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
import me.timothy.bots.database.AdminUpdateMapping;
import me.timothy.bots.models.AdminUpdate;

public class MysqlAdminUpdateMapping extends MysqlObjectMapping<AdminUpdate> implements AdminUpdateMapping {
	private static final Logger logger = LogManager.getLogger();
	
	public MysqlAdminUpdateMapping(LoansDatabase database, Connection connection) {
		super(database, connection, "admin_updates",
				new MysqlColumn(Types.INTEGER, "id", true),
				new MysqlColumn(Types.INTEGER, "loan_id"),
				new MysqlColumn(Types.INTEGER, "user_id"),
				new MysqlColumn(Types.LONGVARCHAR, "reason"),
				new MysqlColumn(Types.INTEGER, "old_lender_id"),
				new MysqlColumn(Types.INTEGER, "old_borrower_id"),
				new MysqlColumn(Types.INTEGER, "old_principal_cents"),
				new MysqlColumn(Types.INTEGER, "old_principal_repayment_cents"),
				new MysqlColumn(Types.BIT, "old_unpaid"),
				new MysqlColumn(Types.BIT, "old_deleted"),
				new MysqlColumn(Types.LONGVARCHAR, "old_deleted_reason"),
				new MysqlColumn(Types.INTEGER, "new_lender_id"),
				new MysqlColumn(Types.INTEGER, "new_borrower_id"),
				new MysqlColumn(Types.INTEGER, "new_principal_cents"),
				new MysqlColumn(Types.INTEGER, "new_principal_repayment_cents"),
				new MysqlColumn(Types.BIT, "new_unpaid"),
				new MysqlColumn(Types.BIT, "new_deleted"),
				new MysqlColumn(Types.LONGVARCHAR, "new_deleted_reason"),
				new MysqlColumn(Types.TIMESTAMP, "created_at"),
				new MysqlColumn(Types.TIMESTAMP, "updated_at")
				);
	}

	@Override
	public void save(AdminUpdate a) throws IllegalArgumentException {
		if(!a.isValid()) {
			throw new IllegalArgumentException(a + " is not valid");
		}
		
		if(a.createdAt != null) { a.createdAt.setNanos(0); }
		if(a.updatedAt != null) { a.updatedAt.setNanos(0); }
		try {
			PreparedStatement statement;
			if(a.id > 0) {
				statement = connection.prepareStatement("UPDATE admin_updates SET loan_id=?, user_id=?, reason=?, "
						+ "old_lender_id=?, old_borrower_id=?, old_principal_cents=?, old_principal_repayment_cents=?, "
						+ "old_unpaid=?, old_deleted=?, old_deleted_reason=?, new_lender_id=?, new_borrower_id=?, "
						+ "new_principal_cents=?, new_principal_repayment_cents=?, new_unpaid=?, new_deleted=?, "
						+ "new_deleted_reason=?, created_at=?, updated_at=? WHERE id=?");
			}else {
				statement = connection.prepareStatement("INSERT INTO admin_updates "
						+ "(loan_id, user_id, reason, old_lender_id, old_borrower_id, old_principal_cents, "
						+ "old_principal_repayment_cents, old_unpaid, old_deleted, old_deleted_reason, "
						+ "new_lender_id, new_borrower_id, new_principal_cents, new_principal_repayment_cents, "
						+ "new_unpaid, new_deleted, new_deleted_reason, created_at, updated_at) VALUES(?, ?,"
						+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			int counter = 1;
			statement.setInt(counter++, a.loanId);
			statement.setInt(counter++, a.userId);
			statement.setString(counter++, a.reason);
			statement.setInt(counter++, a.oldLenderId);
			statement.setInt(counter++, a.oldBorrowerId);
			statement.setInt(counter++, a.oldPrincipalCents);
			statement.setInt(counter++, a.oldPrincipalRepaymentCents);
			statement.setBoolean(counter++, a.oldUnpaid);
			statement.setBoolean(counter++,  a.oldDeleted);
			statement.setString(counter++, a.oldDeletedReason);
			statement.setInt(counter++, a.newLenderId);
			statement.setInt(counter++, a.newBorrowerId);
			statement.setInt(counter++, a.newPrincipalCents);;
			statement.setInt(counter++,  a.newPrincipalRepaymentCents);
			statement.setBoolean(counter++, a.newUnpaid);
			statement.setBoolean(counter++, a.newDeleted);
			statement.setString(counter++, a.newDeletedReason);
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
					throw new RuntimeException("Expected generated keys from admin_updates, but didn't get any!");
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
	public List<AdminUpdate> fetchAll() {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM admin_updates");
			
			ResultSet results = statement.executeQuery();
			List<AdminUpdate> adminUpdates = new ArrayList<>();
			while(results.next()) {
				adminUpdates.add(fetchFromSet(results));
			}
			results.close();
			
			statement.close();
			return adminUpdates;
		}catch(SQLException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Fetches the admin update in the current row in the set
	 * @param results the set
	 * @return the admin update in the current row
	 * @throws SQLException if one occurs
	 */
	protected AdminUpdate fetchFromSet(ResultSet results) throws SQLException {
		return new AdminUpdate(results.getInt("id"), results.getInt("loan_id"), results.getInt("user_id"), 
				results.getString("reason"), results.getInt("old_lender_id"), results.getInt("old_borrower_id"), 
				results.getInt("old_principal_cents"), results.getInt("old_principal_repayment_cents"), 
				results.getBoolean("old_unpaid"), results.getBoolean("old_deleted"), results.getString("old_deleted_reason"), 
				results.getInt("new_lender_id"), results.getInt("new_borrower_id"), results.getInt("new_principal_cents"), 
				results.getInt("new_principal_repayment_cents"), results.getBoolean("new_unpaid"), results.getBoolean("new_deleted"), 
				results.getString("new_deleted_reason"), results.getTimestamp("created_at"), results.getTimestamp("updated_at"));
	}
	@Override
	protected void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE admin_updates ("
				+ "id INT NOT NULL AUTO_INCREMENT, "
				+ "loan_id INT NOT NULL, "
				+ "user_id INT NOT NULL, "
				+ "reason TEXT, "
				+ "old_lender_id INT NOT NULL, "
				+ "old_borrower_id INT NOT NULL, "
				+ "old_principal_cents INT NOT NULL, "
				+ "old_principal_repayment_cents INT NOT NULL, "
				+ "old_unpaid TINYINT(1) NOT NULL DEFAULT 0, "
				+ "old_deleted TINYINT(1) NOT NULL DEFAULT 0, "
				+ "old_deleted_reason TEXT, "
				+ "new_lender_id INT NOT NULL, "
				+ "new_borrower_id INT NOT NULL, "
				+ "new_principal_cents INT NOT NULL, "
				+ "new_principal_repayment_cents INT NOT NULL, "
				+ "new_unpaid TINYINT(1) NOT NULL DEFAULT 0, "
				+ "new_deleted TINYINT(1) NOT NULL DEFAULT 0, "
				+ "new_deleted_reason TEXT, "
				+ "created_at TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01', "
				+ "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (id), "
				+ "INDEX ind_admupd_loan_id (loan_id), "
				+ "INDEX ind_admupd_user_id (user_id), "
				+ "INDEX ind_admupd_olender_id (old_lender_id), "
				+ "INDEX ind_admupd_oborrow_id (old_borrower_id), "
				+ "INDEX ind_admupd_nlender_id (new_lender_id), "
				+ "INDEX ind_admupd_nborrow_id (new_borrower_id), "
				+ "FOREIGN KEY (loan_id) REFERENCES loans(id), "
				+ "FOREIGN KEY (user_id) REFERENCES users(id), "
				+ "FOREIGN KEY (old_lender_id) REFERENCES users(id), "
				+ "FOREIGN KEY (old_borrower_id) REFERENCES users(id), "
				+ "FOREIGN KEY (new_lender_id) REFERENCES users(id), "
				+ "FOREIGN KEY (new_borrower_id) REFERENCES users(id)"
				+ ")");
		statement.close();
	}

}
