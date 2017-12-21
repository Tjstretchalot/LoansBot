package me.timothy.bots.models;

/**
 * Many-to-Many relationship between saved queries and users
 * 
 * @author Timothy
 */
public class SavedQueryUser {
	public int id;
	public int saved_query_id;
	public int user_id;
	public boolean owned;
	public boolean inverse;
	
	/**
	 * @param id the unique database id
	 * @param saved_query_id the id for the saved query
	 * @param user_id the id for the user who has the query on their list
	 * @param owned if the user owns the query
	 * @param inverse if this is negating an alwaysShared query
	 */
	public SavedQueryUser(int id, int saved_query_id, int user_id, boolean owned, boolean inverse) {
		super();
		this.id = id;
		this.saved_query_id = saved_query_id;
		this.user_id = user_id;
		this.owned = owned;
		this.inverse = inverse;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + (inverse ? 1231 : 1237);
		result = prime * result + (owned ? 1231 : 1237);
		result = prime * result + saved_query_id;
		result = prime * result + user_id;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SavedQueryUser))
			return false;
		SavedQueryUser other = (SavedQueryUser) obj;
		if (id != other.id)
			return false;
		if (inverse != other.inverse)
			return false;
		if (owned != other.owned)
			return false;
		if (saved_query_id != other.saved_query_id)
			return false;
		if (user_id != other.user_id)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "SavedQueryUser [id=" + id + ", saved_query_id=" + saved_query_id + ", user_id=" + user_id + ", owned="
				+ owned + ", inverse=" + inverse + "]";
	}
	
	
}
