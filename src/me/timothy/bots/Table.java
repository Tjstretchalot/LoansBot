package me.timothy.bots;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a table for reddit formatting
 * 
 * @author Timothy
 *
 */
public class Table {
	/**
	 * Contains the name & alignment of the column
	 * 
	 * @author Timothy
	 */
	public static class ColumnName {
		/**
		 * Name of the column
		 */
		protected String name;
		
		/**
		 * Alignment of the column
		 */
		protected Alignment alignment;
		
		/**
		 * Creates a column name with the specified name and alignment
		 * @param nm the name
		 * @param alignment the alignment
		 */
		public ColumnName(String nm, Alignment alignment) {
			this.name = nm;
			this.alignment = alignment;
		}
	}
	
	/**
	 * Describes an alignment in a column
	 * 
	 * @author Timothy
	 */
	public static enum Alignment {
		LEFT(":--"), CENTER(":-:"), RIGHT("--:");
		
		
		private final String string;
		
		/**
		 * Creates an alignment that is conveyed to
		 * reddit using the specified string
		 * @param str the string
		 */
		Alignment(String str) {
			this.string = str;
		}
		
		/**
		 * Gets the alignments string value for reddit
		 * @return the alignments string value for reddit
		 */
		public String getString() {
			return string;
		}
	}
	
	private ColumnName[] columnNames;
	private List<String[]> rows;
	
	/**
	 * Creates a table
	 * @param columnNames the names of each column
	 */
	public Table(ColumnName... columnNames) {
		init(columnNames);
	}
	
	/**
	 * Creates a table with the specified column names, all with the same alignment
	 * @param alignment the alignment
	 * @param columns the columns
	 */
	public Table(Alignment alignment, String... columns) {
		ColumnName[] cols = new ColumnName[columns.length];
		
		for(int i = 0; i < columns.length; i++) {
			cols[i] = new ColumnName(columns[i], alignment);
		}
		
		init(cols);
	}
	
	/**
	 * Initializes the table with the specified column names
	 * @param columnNames the column names
	 */
	private void init(ColumnName... columnNames) {
		if(columnNames.length == 0)
			throw new IllegalArgumentException("A table needs at least 1 column!");
		
		this.columnNames = columnNames;
		rows = new ArrayList<>();
	}
	
	/**
	 * Adds the row to the list
	 * @param cols the columns in the row
	 */
	public void addRow(String... cols) {
		if(cols.length != columnNames.length)
			throw new IllegalArgumentException(String.format("Expected %d columns but got %d", columnNames.length, cols.length));
		
		rows.add(cols);
	}
	
	/**
	 * Gets the reddit format of the table
	 * @return the reddit format of the table
	 */
	public String format() {
		StringBuilder result = new StringBuilder();
		
		boolean first = true;
		for(ColumnName nm : columnNames) {
			if(!first)
				result.append("|");
			else
				first = false;
			
			result.append(nm.name);
		}
		result.append("\n");
		first = true;
		for(ColumnName nm : columnNames) {
			if(!first)
				result.append("|");
			else
				first = false;
			
			result.append(nm.alignment.getString());
		}
		result.append("\n");
		
		for(String[] row : rows) {
			first = true;
			
			for(String col : row) {
				if(!first)
					result.append("|");
				else
					first = false;
				
				result.append(col);
			}
			
			result.append("\n");
		}
		
		return result.toString();
	}
}
