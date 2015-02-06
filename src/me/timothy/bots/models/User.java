package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a user
 * 
 * @author Timothy
 */

public class User {
	public int id;
	public String username;
	public int auth;
	public String passwordDigest;
	public boolean claimed;
	public String claimCode;
	public Timestamp claimLinkSetAt;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public String email;
	public String name;
	public String streetAddress;
	public String city;
	public String state;
	public String zip;
	public String country;
  
	public User(int id, String username, int auth, String passwordDigest, boolean claimed, String claimCode, Timestamp claimLinkSetAt, Timestamp createdAt,
			Timestamp updatedAt, String email, String name, String streetAddress, String city, String state, String zip, String country) {
		this.id = id;
		this.username = username;
		this.auth = auth;
		this.passwordDigest = passwordDigest;
		this.claimed = claimed;
		this.claimCode = claimCode;
		this.claimLinkSetAt = claimLinkSetAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		
		this.email = email;
		this.name = name;
		this.streetAddress = streetAddress;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country = country;
	}
	
	public User(String username) {
		this(-1, username, 0, null, false, null, null, null, null, null, null, null, null, null, null, null);
	}
	
	public User() {
		this(null);
	}
	
	/**
	 * Verifies the username is not null, and that
	 * created at / updated at are not null
	 * 
	 * @return if this user is not obviously wrong
	 */
	public boolean isValid() {
		if(username == null)
			return false;
		else if(createdAt == null) 
			return false;
		else if(updatedAt == null)
			return false;
		return true;
	}
}
