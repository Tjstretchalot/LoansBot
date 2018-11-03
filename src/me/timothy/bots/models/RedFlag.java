package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * A red flag that is on a red flag report. Very generic so it's easy to 
 * add new red flags later.
 * 
 * @author Timothy
 */
public class RedFlag {
	/**
	 * Though the database stores these just as strings, in Java we centralize all
	 * the type options right here. The names seem duplicated but its so we can
	 * refactor without changing the database identifier
	 * 
	 * @author Timothy
	 */
	public enum RedFlagType {
		/**
		 * This red flag is generated when we detect the user has nuked his
		 * account history
		 */
		NUKED_HISTORY("NUKED_HISTORY"),
		
		SUBREDDIT("SUBREDDIT"),
		
		ACTIVITY_GAP("ACTIVITY_GAP")
		
		;
		
		/**
		 * The identifier for this red flag type in the database.
		 */
		public final String databaseIdentifier;
		
		private RedFlagType(String dbIden) {
			databaseIdentifier = dbIden;
		}
		
		/**
		 * Gets the red flag type associated with the given database
		 * identifier.
		 * 
		 * @param iden the database identifier
		 * @return the corresponding red flag type 
		 * @throws RuntimeException if no such red flag exists
		 */
		public static RedFlagType fromIdentifier(String iden) {
			RedFlagType[] types = RedFlagType.values();
			
			for(RedFlagType rft : types) {
				if(rft.databaseIdentifier.equals(iden))
					return rft;
			}
			
			throw new RuntimeException(iden);
		}
	}
	
	public int id;
	public int reportId;
	public RedFlagType type;
	/**
	 * The unique identifier is used to identify if we raised the same flag multiple times. It is unique 
	 * to the report type. Ex: for use of /r/drugs it might be SUBREDDIT|drugs|Usage of /r/drugs 
	 */
	public String identifier;
	public String description;
	public int count;
	public Timestamp createdAt;
	
	/**
	 * Create a new red flag 
	 * @param id the database id or -1 if not in the database yet
	 * @param reportId the id of the RedFlagReport this is for
	 * @param type the type of red flag this is
	 * @param identifier an identifier for this flag for lookup
	 * @param description the description markdown to be posted on reddit
	 * @param count how many of this flag have been generated
	 * @param createdAt when this red flag was created
	 */
	public RedFlag(int id, int reportId, RedFlagType type, String identifier, String description, int count, Timestamp createdAt) {
		this.id = id;
		this.reportId = reportId;
		this.type = type;
		this.identifier = identifier;
		this.description = description;
		this.count = count;
		this.createdAt = createdAt;
	}
	
	/**
	 * Create a new red flag with the database identifier for the type
	 * @param id the database id or -1 if not in the database yet
	 * @param reportId the id of the RedFlagReport this is for
	 * @param type the type of red flag this is
	 * @param identifier an identifier for this flag for lookup
	 * @param description the description markdown to be posted on reddit
	 * @param count how many of this flag have been generated
	 * @param createdAt when this red flag was created
	 */
	public RedFlag(int id, int reportId, String type, String identifier, String description, int count, Timestamp createdAt) {
		this(id, reportId, RedFlagType.fromIdentifier(type), identifier, description, count, createdAt);
	}
	
	/**
	 * Determine if this is reasonably ready to be saved into the database.
	 * @return if this can maybe be saved
	 */
	public boolean isValid() {
		return (reportId > 0 && type != null && identifier != null && description != null && count > 0 && createdAt != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + reportId;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RedFlag))
			return false;
		RedFlag other = (RedFlag) obj;
		if (count != other.count)
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (reportId != other.reportId)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RedFlag [id=" + id + ", reportId=" + reportId + ", type=" + type + ", identifier=" + identifier
				+ ", description=" + description + ", count=" + count + ", createdAt=" + createdAt + "]";
	}
	
	
}
