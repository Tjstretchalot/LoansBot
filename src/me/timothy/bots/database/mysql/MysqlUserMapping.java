package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.UserMapping;
import me.timothy.bots.models.User;

public class MysqlUserMapping extends MysqlObjectMapping<User> implements UserMapping {

	public MysqlUserMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(User a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public User fetchById(int id) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public User fetchOrCreateByName(String username) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int fetchMaxUserId() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<User> fetchUsersToSendCode() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<User> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

}
