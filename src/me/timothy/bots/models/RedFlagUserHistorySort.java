package me.timothy.bots.models;

/**
 * We want to process the history for a user in a specific order, but
 * we don't want to do an offset in our limit because we'll get to be
 * verry slow if we pause many reports and resume near the end.
 * 
 * This describes a table which gives us order for the links and comments.
 * Once you have this produced you don't have to worry about paginating
 * with time (troublesome because of the low granularity that we save timestamps
 * at) 
 * 
 * @author Timothy
 */
public class RedFlagUserHistorySort {
	public enum RedFlagUserThing {
		Comment(1),
		Link(2),
		
		;
		
		/** The value you would see in the database to correspond with this enum */
		public final int value;
		
		private RedFlagUserThing(int value) {
			this.value = value;
		}
		
		/**
		 * Get the thing that corresponds with the given database value
		 * @param value the value in the database
		 * @return the corresponding enum instance
		 */
		public static RedFlagUserThing getByValue(int value) {
			if(value == Comment.value)
				return Comment;
			else if(value == Link.value)
				return Link;
			
			throw new IllegalArgumentException("Unexpected value: " + value);
		}
	}
	/** Row identifier */
	public int id;
	/** The report this corresponds to */
	public int reportId;
	/** The integer sort value for the referenced row */
	public int sort;
	/** The referenced table */
	public RedFlagUserThing table;
	/** The id of the thing in the other table */
	public int foreignId;
	
	/**
	 * Create a new instance of a red flag user history sort
	 * 
	 * @param id the id in the database, or -1 if not in the database yet
	 * @param reportId the id of the report
	 * @param sort an integer ordering number 
	 * @param table the table that the real thing can be found in
	 * @param foreignId the id in the other table
	 */
	public RedFlagUserHistorySort(int id, int reportId, int sort, RedFlagUserThing table, int foreignId) {
		this.id = id;
		this.reportId = reportId;
		this.sort = sort;
		this.table = table;
		this.foreignId = foreignId;
	}
	
	/** 
	 * Determines if this is potentially valid
	 * @return true if this is maybe a valid row
	 */
	public boolean isValid() {
		return (reportId > 0 && sort > 0 && table != null && foreignId > 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + foreignId;
		result = prime * result + id;
		result = prime * result + reportId;
		result = prime * result + sort;
		result = prime * result + ((table == null) ? 0 : table.hashCode());
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
		RedFlagUserHistorySort other = (RedFlagUserHistorySort) obj;
		if (foreignId != other.foreignId)
			return false;
		if (id != other.id)
			return false;
		if (reportId != other.reportId)
			return false;
		if (sort != other.sort)
			return false;
		if (table != other.table)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RedFlagUserHistorySort [id=" + id + ", reportId=" + reportId + ", sort=" + sort + ", table=" + table
				+ ", foreign_id=" + foreignId + "]";
	}
	
	
}
