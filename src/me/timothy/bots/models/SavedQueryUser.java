package me.timothy.bots.models;

/**
 * Many-to-Many relationship between saved queries and users
 * 
 * @author Timothy
 */
public class SavedQueryUser {
	public int id;
	public int savedQueryId;
	public int userId;
	public boolean owned;
	public boolean inverse;
	
	/**
	 * @param id the unique database id
	 * @param savedQueryId the id for the saved query
	 * @param userId the id for the user who has the query on their list
	 * @param owned if the user owns the query
	 * @param inverse if this is negating an alwaysShared query
	 */
	public SavedQueryUser(int id, int savedQueryId, int userId, boolean owned, boolean inverse) {
		super();
		this.id = id;
		this.savedQueryId = savedQueryId;
		this.userId = userId;
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
		result = prime * result + savedQueryId;
		result = prime * result + userId;
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
		if (savedQueryId != other.savedQueryId)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "SavedQueryUser [id=" + id + ", saved_query_id=" + savedQueryId + ", user_id=" + userId + ", owned="
				+ owned + ", inverse=" + inverse + "]";
	}
	
	
}
