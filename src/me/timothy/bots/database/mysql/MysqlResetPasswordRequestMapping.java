package me.timothy.bots.database.mysql;

import java.sql.Connection;
import java.util.List;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.ResetPasswordRequestMapping;
import me.timothy.bots.models.ResetPasswordRequest;

public class MysqlResetPasswordRequestMapping extends MysqlObjectMapping<ResetPasswordRequest> implements ResetPasswordRequestMapping {

	public MysqlResetPasswordRequestMapping(LoansDatabase database, Connection connection) {
		super(database, connection);
	}

	@Override
	public void save(ResetPasswordRequest a) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<ResetPasswordRequest> fetchUnsent() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public List<ResetPasswordRequest> fetchAll() {
		throw new RuntimeException("Not yet implemented");
	}

}
