package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.WarningMapping;
import me.timothy.bots.models.Warning;

public class MysqlWarningMapping extends MysqlObjectMapping<Warning> implements WarningMapping {

	public MysqlWarningMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Warning a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Warning> fetchByUserId(int userId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Warning> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

}
