package me.timothy.bots.database.mysql;

import java.sql.Connection;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.FullnameMapping;
import me.timothy.bots.models.Fullname;

public class MysqlFullnameMapping extends MysqlObjectMapping<Fullname> implements FullnameMapping {

	public MysqlFullnameMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Fullname a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean contains(String fullname) {
		throw new RuntimeException("Not yet implemented");
	}

}
