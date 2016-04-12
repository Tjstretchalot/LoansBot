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

	public Recheck() {
		this(-1, null, null, null);
	}
	
	/**
	 * Does some sanity checking to see if the recheck might
	 * be ready to be saved to a database
	 * @return if this recheck is probably valid
	 */
	public boolean isValid() {
		return (fullname != null) && (createdAt != null) && (updatedAt != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
		result = prime * result + id;
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
		Recheck other = (Recheck) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (fullname == null) {
			if (other.fullname != null)
				return false;
		} else if (!fullname.equals(other.fullname))
			return false;
		if (id != other.id)
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Recheck [id=" + id + ", fullname=" + fullname + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ "]";
	}
	
	
}
