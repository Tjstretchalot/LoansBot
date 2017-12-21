package me.timothy.bots.models;

/**
 * Saved queries are actually broken up into several pieces, so 
 * having only one SavedQueryParam model isn't particularly useful.
 * SavedQueryParam have a "many-to-one" relationship with SavedQuery
 * 
 * This is because there is no firm definition for the parameters that are
 * included in a saved query.
 * 
 * @author Timothy
 */
public class SavedQueryParam {
	public int id;
	public int savedQueryID;
	public String paramName;
	public String paramOptionsJSON;
	
	/**
	 * @param id The unique key that can be used to identify this row
	 * @param savedQueryID the main saved query id
	 * @param paramName the name for the param name that this model contains
	 * @param paramOptionsJSON the options that are specified, in JSON
	 */
	public SavedQueryParam(int id, int savedQueryID, String paramName, String paramOptionsJSON) {
		this.id = id;
		this.savedQueryID = savedQueryID;
		this.paramName = paramName;
		this.paramOptionsJSON = paramOptionsJSON;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
		result = prime * result + ((paramOptionsJSON == null) ? 0 : paramOptionsJSON.hashCode());
		result = prime * result + savedQueryID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SavedQueryParam))
			return false;
		SavedQueryParam other = (SavedQueryParam) obj;
		if (id != other.id)
			return false;
		if (paramName == null) {
			if (other.paramName != null)
				return false;
		} else if (!paramName.equals(other.paramName))
			return false;
		if (paramOptionsJSON == null) {
			if (other.paramOptionsJSON != null)
				return false;
		} else if (!paramOptionsJSON.equals(other.paramOptionsJSON))
			return false;
		if (savedQueryID != other.savedQueryID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SavedQueryParam [id=" + id + ", savedQueryID=" + savedQueryID + ", paramName=" + paramName
				+ ", paramOptionsJSON=" + paramOptionsJSON + "]";
	}
	
}
