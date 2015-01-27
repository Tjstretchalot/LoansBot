package me.timothy.bots.responses;

import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;

/**
 * For when you don't need any fancy formatting information
 * to format things
 * 
 * @author Timothy
 */
public class GenericFormattableObject implements FormattableObject {
	private String string;
	
	public GenericFormattableObject(String string) {
		this.string = string;
	}

	@Override
	public String toFormattedString(ResponseInfo info, String myName, FileConfiguration config, LoansDatabase db) {
		return string;
	}

	@Override
	public String toString() {
		return string;
	}
}
