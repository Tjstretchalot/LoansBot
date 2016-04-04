package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.ResponseHistoryMapping;
import me.timothy.bots.models.ResponseHistory;

public class MysqlResponseHistoryMapping extends MysqlObjectMapping<ResponseHistory> implements ResponseHistoryMapping {

	public MysqlResponseHistoryMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(ResponseHistory a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<ResponseHistory> fetchForResponse(int responseId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<ResponseHistory> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

}
