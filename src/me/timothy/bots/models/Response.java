package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a response in the database
 * 
 * @author Timothy
 *
 */
public class Response {
	public int id;
	public String name;
	public String responseBody;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public Response(int id, String name, String responseBody, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.name = name;
		this.responseBody = responseBody;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	/**
	 * Checks to make sure the response can be put
	 * in the database without breaking things
	 * 
	 * @return if this response is valid
	 */
	public boolean isValid() {
		return name != null && name.length() <= 255 && responseBody != null && createdAt != null && updatedAt != null;
	}
}
