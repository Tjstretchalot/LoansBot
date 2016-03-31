package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.RecheckMapping;
import me.timothy.bots.models.Recheck;

public class MysqlRecheckMapping extends MysqlObjectMapping<Recheck> implements RecheckMapping {

	public MysqlRecheckMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Recheck a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Recheck> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void delete(Recheck recheck) {
		throw new RuntimeException("Not yet implemented");
	}

}
