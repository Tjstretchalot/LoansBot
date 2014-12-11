package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a recheck
 * 
 * @author Timothy
 */
public class Recheck {
	public int id;
	public String fullname;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public Recheck(int id, String fullname, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.fullname = fullname;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
