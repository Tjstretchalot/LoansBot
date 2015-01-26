package me.timothy.bots.responses;

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
	public String toFormattedString(String myName, ResponseInfo objects) {
		return string;
	}

}
