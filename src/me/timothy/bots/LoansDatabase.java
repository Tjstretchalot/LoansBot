package me.timothy.bots;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the connection to the MySQL database, both for remembering loans and
 * remembering reddit Thing fullnames so they are not parsed multiple times. <br>
 * <br>
 * This class connects with 1 database, but may be reused by multiple calls to
 * connect. Every public member function interacts with the underlying database.
 * This class is NOT thread-safe.
 * 
 * @author Timothy
 */
public class LoansDatabase extends Database {
	private Connection connection;

	public LoansDatabase() {

	}

	/**
	 * Connects to the specified database. If there is an active connection
	 * already, the active connection is explicity closed.
	 * 
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param url
	 *            the url
	 * @throws SQLException
	 *             if a sql-related exception occurs
	 */
	public void connect(String username, String password, String url)
			throws SQLException {
		if (connection != null) {
			connection.close();
		}
		connection = DriverManager.getConnection(url, username, password);
	}

	/**
	 * Creates any necessary tables if they do not exist (as if by CREATE TABLE
	 * IF NOT EXISTS). <br>
	 * <br>
	 * This does not ensure that the tables are in the correct format if they
	 * already exist.
	 * 
	 * @throws SQLException
	 *             if a sql-related exception occurs
	 */
	public void ensureTablesExist() throws SQLException {
		Statement statement = connection.createStatement();

		statement.execute("CREATE TABLE IF NOT EXISTS loans ("
				+ "loan_id INT NOT NULL AUTO_INCREMENT, "
				+ "original_thread TEXT, "
				+ "amount_pennies INT NOT NULL, "
				+ "lender TEXT NOT NULL, "
				+ "borrower TEXT NOT NULL, "
				+ "amount_paid_pennies INT NOT NULL, "
				+ "unpaid BOOL NOT NULL DEFAULT 0, "
				+ "date_loan_given_jutc BIGINT DEFAULT 0, "
				+ "date_paid_back_full_jutc BIGINT DEFAULT 0, "
				+ "PRIMARY KEY (loan_id)"
				+ ");");

		statement = connection.createStatement();
		statement.execute("CREATE TABLE IF NOT EXISTS remembered ("
				+ "id CHAR(12) NOT NULL, "
				+ "PRIMARY KEY (id)"
				+ ");");
		
		statement = connection.createStatement();
		statement.execute("CREATE TABLE IF NOT EXISTS applicants (" +
				"id INT NOT NULL AUTO_INCREMENT, " +
				"timestamp TEXT, " +
				"username CHAR(20), " +
				"email TEXT, " +
				"first_name CHAR(50), " +
				"last_name CHAR(50), " +
				"street_address TEXT, " +
				"city CHAR(25), " +
				"zip int, " +
				"state char(10), " +
				"country char(25), " +
				"payment_method char(25), " +
				"method_of_use char(25), " +
				"PRIMARY KEY (id)" +
				");");
	}

	/**
	 * Creates a new loan with the specified information. {@code loan} is
	 * updated to reflect the new loan id. A new loan entry will be created each
	 * time, regardless of if a similar entry already exists in this database.
	 * The loans old id is ignored. <br>
	 * The statement is prepared, and thus safe from SQL-injection.
	 * 
	 * @param loan
	 *            the loan
	 * @throws SQLException
	 *             if a sql-exception occurs
	 */
	public void addLoan(Loan loan) throws SQLException {
		PreparedStatement prep = connection
				.prepareStatement(
						"INSERT INTO loans (original_thread, amount_pennies, lender, borrower, " +
						"amount_paid_pennies, unpaid, date_loan_given_jutc, date_paid_back_full_jutc) " +
						"VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);

		prep.setString(1, loan.getOriginalThread());
		prep.setInt(2, loan.getAmountPennies());
		prep.setString(3, loan.getLender());
		prep.setString(4, loan.getBorrower());
		prep.setInt(5, loan.getAmountPaidPennies());
		prep.setBoolean(6, loan.isUnpaid());
		prep.setLong(7, loan.getDateLoanGivenJUTC());
		prep.setLong(8, loan.getDatePaidBackFullJUTC());

		prep.executeUpdate();

		ResultSet keys = prep.getGeneratedKeys();
		keys.next();
		loan.setId(keys.getInt(1));
		keys.close();
	}

	/**
	 * Pays the specified loan by incrementing amountPaidPennies by
	 * amountPennies. This does not verify that amountPaidPennies is greater
	 * than amountPennies. <br>
	 * <br>
	 * The statement is prepared, and thus safe from SQL-injection.
	 * 
	 * @param loanId
	 *            the loan id to modify
	 * @param amountPennies
	 *            the amount in pennies to increment amountPaidPennies
	 * @throws SQLException
	 *             if a sql-related exception occurs
	 */
	public void payLoan(int loanId, int amountPennies) throws SQLException {
		PreparedStatement prep = connection
				.prepareStatement("UPDATE loans SET amount_paid_pennies = amount_paid_pennies + ? WHERE loan_id=?");

		prep.setInt(1, amountPennies);
		prep.setInt(2, loanId);

		prep.executeUpdate();
	}

	/**
	 * Sets the specified loan id to unpaid/paid
	 * 
	 * @param loanId
	 *            the loan id to modify
	 * @param unpaid
	 *            if true then sets loan ids unpaid to true, otherwise false
	 * @throws SQLException
	 *             if a sql-related exception occurs
	 */
	public void setLoanUnpaid(int loanId, boolean unpaid) throws SQLException {
		PreparedStatement prep = connection
				.prepareStatement("UPDATE loans SET unpaid=? WHERE loan_id=?");

		prep.setBoolean(1, unpaid);
		prep.setInt(2, loanId);

		prep.executeUpdate();
	}

	/**
	 * Sets the specified loan id's paid back in full timestamp in the slightly
	 * modified version of UTC that is System.currentTimeMillis()
	 * 
	 * @param loanId
	 *            the loan id
	 * @param paidBackInFullJUTC
	 *            the timestamp when the loan was paid back in full
	 * @throws SQLException
	 *             if a sql-related exception occurs
	 */
	public void setLoanPaidBackInFullDate(int loanId, long paidBackInFullJUTC)
			throws SQLException {
		PreparedStatement prep = connection
				.prepareStatement("UPDATE loans SET date_paid_back_full_jutc=? WHERE loan_id=?");

		prep.setLong(1, paidBackInFullJUTC);
		prep.setInt(2, loanId);

		prep.executeUpdate();
	}

	/**
	 * Checks for any loans with {@code concernedParty} as the lender or
	 * borrower
	 * 
	 * @param concernedParty
	 *            the concerned party
	 * @return a list of loans (not null) containing {@code concernedParty}
	 * @throws SQLException
	 *             if a sql-related exception occurs
	 */
	public List<Loan> getLoansWith(String concernedParty) throws SQLException {
		List<Loan> result = new ArrayList<>();

		PreparedStatement prep = connection
				.prepareStatement("SELECT * FROM loans WHERE lender=? OR borrower=?");

		prep.setString(1, concernedParty);
		prep.setString(2, concernedParty);

		ResultSet results = prep.executeQuery();
		fillLoans(result, results);
		results.close();
		return result;
	}

	/**
	 * Gets loans with the specified borrower and lender
	 * 
	 * @param borrower
	 *            the borrower
	 * @param lender
	 *            the lender
	 * @return Loans (not null) containing {@code borrower} as the borrower and
	 *         {@code lender} as the lender
	 * @throws SQLException
	 */
	public List<Loan> getLoansWith(String borrower, String lender)
			throws SQLException {
		List<Loan> result = new ArrayList<>();

		PreparedStatement prep = connection
				.prepareStatement("SELECT * FROM loans WHERE lender=? AND borrower=?");

		prep.setString(1, lender);
		prep.setString(2, borrower);

		ResultSet results = prep.executeQuery();
		fillLoans(result, results);
		results.close();
		return result;
	}

	private void fillLoans(List<Loan> result, ResultSet results)
			throws SQLException {
		if (results.first()) {
			do {
				Loan l = new Loan(results.getInt("amount_pennies"),
						results.getString("lender"),
						results.getString("borrower"),
						results.getInt("amount_paid_pennies"),
						results.getBoolean("unpaid"),
						results.getLong("date_loan_given_jutc"),
						results.getLong("date_paid_back_full_jutc"));
				l.setId(results.getInt("loan_id"));
				l.setOriginalThread(results.getString("original_thread"));
				result.add(l);
			} while (results.next());
		}
	}

	public void addFullname(String id) {
		PreparedStatement prep;
		try {
			prep = connection
					.prepareStatement("INSERT INTO remembered (id) VALUES(?)");

			prep.setString(1, id);

			prep.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean containsFullname(String id) {
		try {
			PreparedStatement prep = connection
					.prepareStatement("SELECT * FROM remembered WHERE id=?");

			prep.setString(1, id);

			ResultSet results = prep.executeQuery();
			boolean hasFirst = results.first();
			results.close();

			return hasFirst;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets any applicants in the database that have the specified
	 * reddit username.
	 * 
	 * @param username the username to search for
	 * @return applicants with that reddit username
	 */
	public List<Applicant> getApplicantByUsername(String username) {
		PreparedStatement prep;
		
		try {
			prep = connection.prepareStatement("SELECT * FROM applicants WHERE username=?");
			prep.setString(1, username);
			ResultSet results = prep.executeQuery();
			return getApplicants(results);
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Gets any applicants in the database that match the specified 
	 * information (case insensitive)
	 * @param user
	 * @param firstName
	 * @param lastName
	 * @param street
	 * @param city
	 * @param state
	 * @param country
	 * @return applicants with information like the arguments
	 */
	public List<Applicant> getApplicantsByInfo(String user, String firstName, String lastName, String street, String city, String state, String country) {
		PreparedStatement prep;
		try {
			prep = connection.prepareStatement("SELECT * FROM applicants WHERE username LIKE ? AND first_name LIKE ? AND last_name LIKE ? AND street_address LIKE ? AND city LIKE ? AND state LIKE ? AND country LIKE ?");
			prep.setString(1, user);
			prep.setString(2, firstName);
			prep.setString(3, lastName);
			prep.setString(4, street);
			prep.setString(5, city);
			prep.setString(6, state);
			prep.setString(7, country);
			ResultSet results = prep.executeQuery();
			return getApplicants(results);
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Adds an applicant to the database
	 * @param applicant the applicant to add
	 */
	public void addApplicant(Applicant applicant) {
		PreparedStatement prep;
		
		try {
			prep = connection.prepareStatement("INSERT INTO applicants (timestamp, username, email, first_name, last_name, street_address, " +
					"city, zip, state, country, payment_method, method_of_use) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			prep.setString(1, applicant.getTimestamp());
			prep.setString(2, applicant.getUsername());
			prep.setString(3, applicant.getEmail());
			prep.setString(4, applicant.getFirstName());
			prep.setString(5, applicant.getLastName());
			prep.setString(6, applicant.getStreetAddress());
			prep.setString(7, applicant.getCity());
			prep.setInt(8, applicant.getZip());
			prep.setString(9, applicant.getState());
			prep.setString(10, applicant.getCountry());
			prep.setString(11, applicant.getPaymentMethod());
			prep.setString(12, applicant.getMainMethodOfUse());
		}catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Gets applicants from the result set, assumes * was used for wat to retrieve
	 * 
	 * @param results the results to parse
	 * @return the parsed applicant opjects
	 * @throws SQLException if a sql-exception occurs
	 */
	private List<Applicant> getApplicants(ResultSet results) throws SQLException {
		List<Applicant> result = new ArrayList<>();
		
		while(results.next()) {
			result.add(new Applicant(
					results.getString("timestamp"), 
					results.getString("username"),
					results.getString("email"), 
					results.getString("first_name"), 
					results.getString("last_name"),
					results.getString("street_address"),
					results.getString("city"),
					results.getInt("zip"),
					results.getString("state"),
					results.getString("country"),
					results.getString("payment_method"),
					results.getString("main_method_of_use")));
			
			result.get(result.size() - 1).setId(results.getInt("id"));
		}
		
		return result;
	}
	public static void initMysql() throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
	}
}
