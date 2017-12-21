package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Contains the meta information about a saved query. The actual parameters
 * and their options are split into the "SavedQueryParam" which has a many-to-one
 * relationship with SavedQuery.
 * 
 * @author Timothy
 */
public class SavedQuery {
	public int id;
	public String name;
	public boolean shared;
	public boolean alwaysShared;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	/**
	 * @param id unique database identifier
	 * @param name the name 
	 * @param shared if anyone can view this query
	 * @param alwaysShared if everyone always has this query on their list
	 * @param createdAt when this query was created
	 * @param updatedAt when this query was last updated
	 */
	public SavedQuery(int id, String name, boolean shared, boolean alwaysShared, Timestamp createdAt, Timestamp updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.shared = shared;
		this.alwaysShared = alwaysShared;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (alwaysShared ? 1231 : 1237);
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (shared ? 1231 : 1237);
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SavedQuery))
			return false;
		SavedQuery other = (SavedQuery) obj;
		if (alwaysShared != other.alwaysShared)
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (shared != other.shared)
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
		return "SavedQuery [id=" + id + ", name=" + name + ", shared=" + shared + ", alwaysShared=" + alwaysShared + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
	
}
