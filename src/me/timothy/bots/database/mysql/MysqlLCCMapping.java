package me.timothy.bots.database.mysql;

import java.sql.Connection;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.LCCMapping;
import me.timothy.bots.models.LendersCampContributor;

public class MysqlLCCMapping extends MysqlObjectMapping<LendersCampContributor> implements LCCMapping {

	public MysqlLCCMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(LendersCampContributor a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean contains(int userId) {
		throw new RuntimeException("Not yet implemented");
	}

}
