package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.ShareCodeMapping;
import me.timothy.bots.models.ShareCode;

public class MysqlShareCodeMapping extends MysqlObjectMapping<ShareCode> implements ShareCodeMapping {

	public MysqlShareCodeMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(ShareCode a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<ShareCode> fetchForUser(int userId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void delete(ShareCode shareCode) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<ShareCode> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

}
