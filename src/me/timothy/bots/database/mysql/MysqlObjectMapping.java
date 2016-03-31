package me.timothy.bots.database.mysql;


import java.sql.Connection;

import me.timothy.bots.LoansDatabase;
import me.timothy.bots.database.ObjectMapping;

/**
 * Describes an ObjectMapping based on mysql
 * 
 * @author Timothy
 *
 * @param <A> What this mapping maps
 */
public abstract class MysqlObjectMapping<A> implements ObjectMapping<A> {
	/**
	 * Using other mappings is discouraged since it couples mappings, however
	 * the practicality occasionally offsets the principle here.
	 */
	protected LoansDatabase database;
	protected Connection connection;
	
	/**
	 * Sets the {@code connection} to the specified connection and the 
	 * {@code database} to the specified database
	 * 
	 * @param database the database 
	 * @param connection the mysql connection
	 */
	protected MysqlObjectMapping(LoansDatabase database, Connection connection) {
		this.database = database;
		this.connection = connection;
	}
}
