package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.AdminUpdateMapping;
import me.timothy.bots.models.AdminUpdate;

public class MysqlAdminUpdateMapping extends MysqlObjectMapping<AdminUpdate> implements AdminUpdateMapping {

	public MysqlAdminUpdateMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(AdminUpdate a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<AdminUpdate> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

}
