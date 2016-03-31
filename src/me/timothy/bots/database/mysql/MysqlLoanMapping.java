package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.LoanMapping;
import me.timothy.bots.models.Loan;

public class MysqlLoanMapping extends MysqlObjectMapping<Loan> implements LoanMapping {

	public MysqlLoanMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Loan a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Integer> fetchLenderIdsWithNewLoanSince(Timestamp timestamp) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Loan> fetchWithBorrowerAndOrLender(int borrowerId, int lenderId, boolean strict) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int fetchNumberOfLoansWithUserAsLender(int lenderId) {
		throw new RuntimeException("Not yet implemented");
	}

}
