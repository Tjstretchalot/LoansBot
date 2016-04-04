package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.ResponseMapping;
import me.timothy.bots.models.Response;

public class MysqlResponseMapping extends MysqlObjectMapping<Response> implements ResponseMapping {

	public MysqlResponseMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(Response a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Response fetchByName(String name) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<Response> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

}
