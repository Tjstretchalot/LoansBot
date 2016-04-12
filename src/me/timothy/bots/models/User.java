package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a user
 * 
 * @author Timothy
 */

public class User {
	public int id;
	public int auth;
	public String passwordDigest;
	public boolean claimed;
	public String claimCode;
	public Timestamp claimLinkSentAt;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	public String email;
	public String name;
	public String streetAddress;
	public String city;
	public String state;
	public String zip;
	public String country;
  
	public User(int id, int auth, String passwordDigest, boolean claimed, String claimCode, Timestamp claimLinkSetAt, Timestamp createdAt,
			Timestamp updatedAt, String email, String name, String streetAddress, String city, String state, String zip, String country) {
		this.id = id;
		this.auth = auth;
		this.passwordDigest = passwordDigest;
		this.claimed = claimed;
		this.claimCode = claimCode;
		this.claimLinkSentAt = claimLinkSetAt;
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
	
	public User() {
		this(-1, 0, null, false, null, null, null, null, null, null, null, null, null, null, null);
	}
	
	/**
	 * Verifies the username is not null, and that
	 * created at / updated at are not null
	 * 
	 * @return if this user is not obviously wrong
	 */
	public boolean isValid() {
		if(createdAt == null) 
			return false;
		else if(updatedAt == null)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + auth;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((claimCode == null) ? 0 : claimCode.hashCode());
		result = prime * result + ((claimLinkSentAt == null) ? 0 : claimLinkSentAt.hashCode());
		result = prime * result + (claimed ? 1231 : 1237);
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((passwordDigest == null) ? 0 : passwordDigest.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((streetAddress == null) ? 0 : streetAddress.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (auth != other.auth)
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (claimCode == null) {
			if (other.claimCode != null)
				return false;
		} else if (!claimCode.equals(other.claimCode))
			return false;
		if (claimLinkSentAt == null) {
			if (other.claimLinkSentAt != null)
				return false;
		} else if (!claimLinkSentAt.equals(other.claimLinkSentAt))
			return false;
		if (claimed != other.claimed)
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (passwordDigest == null) {
			if (other.passwordDigest != null)
				return false;
		} else if (!passwordDigest.equals(other.passwordDigest))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (streetAddress == null) {
			if (other.streetAddress != null)
				return false;
		} else if (!streetAddress.equals(other.streetAddress))
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		if (zip == null) {
			if (other.zip != null)
				return false;
		} else if (!zip.equals(other.zip))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", auth=" + auth + ", passwordDigest=" + passwordDigest + ", claimed=" + claimed
				+ ", claimCode=" + claimCode + ", claimLinkSentAt=" + claimLinkSentAt + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", email=" + email + ", name=" + name + ", streetAddress="
				+ streetAddress + ", city=" + city + ", state=" + state + ", zip=" + zip + ", country=" + country + "]";
	}
	
	
}
