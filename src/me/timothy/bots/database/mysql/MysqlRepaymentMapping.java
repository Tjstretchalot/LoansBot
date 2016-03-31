package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.RepaymentMapping;
import me.timothy.bots.models.Repayment;

public class MysqlRepaymentMapping extends MysqlObjectMapping<Repayment> implements RepaymentMapping {

	public MysqlRepaymentMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Repayment a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Repayment> fetchByLoanId(int loanId) {
		throw new RuntimeException("Not yet implemented");
	}

}
