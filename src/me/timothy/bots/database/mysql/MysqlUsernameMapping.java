package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.UsernameMapping;
import me.timothy.bots.models.Username;

public class MysqlUsernameMapping extends MysqlObjectMapping<Username> implements UsernameMapping {

	public MysqlUsernameMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Username a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Username fetchById(int usernameId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Username> fetchByUserId(int userId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Username fetchByUsername(String username) {
		throw new RuntimeException("Not yet implemented");
	}

}
