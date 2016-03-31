package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.CreationInfoMapping;
import me.timothy.bots.models.CreationInfo;

public class MysqlCreationInfoMapping extends MysqlObjectMapping<CreationInfo> implements CreationInfoMapping {

	public MysqlCreationInfoMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(CreationInfo a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public CreationInfo fetchById(int id) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public CreationInfo fetchByLoanId(int loanId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<CreationInfo> fetchManyByLoanIds(int... loanIds) {
		throw new RuntimeException("Not yet implemented");
	}

}
