package me.timothy.bots.summon.patterns;

import me.timothy.bots.responses.FormattableObject;
import me.timothy.bots.responses.GenericFormattableObject;

/**
 * Describes a string token that may be quoted to include spaces. Example:
 * 
 * $send "how you doing" 3
 * 
 * Here we see that "how you doing" is meant to all be one parameter and the 3 is
 * perhaps another token. If this is optional then quotes must be included, otherwise
 * no quotes is ok.
 * 
 * Supports single quotes and double quotes
 * 
 * $send boo 1
 * 
 * @author Timothy
 */
public class QuotedStringToken implements ISummonToken, IExplicitTerminatingToken {
	private static final char DOUBLE_QUOTE = '"';
	private static final char SINGLE_QUOTE = '\'';
	
	private String id;
	private boolean optional;
	
	private boolean sawStartingQuote;
	private boolean sawEnd;
	private boolean doubleQuote;
	
	private StringBuilder value;
	
	public QuotedStringToken(String id, boolean optional) {
		this.id = id;
		this.optional = optional;
	}

	
	@Override
	public String id() {
		return id;
	}
	
	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public boolean start(char c) {
		sawEnd = false;
		
		if(c == SINGLE_QUOTE) {
			sawStartingQuote = true;
			doubleQuote = false;
			value = new StringBuilder();
			return true;
		}
		
		if(c == DOUBLE_QUOTE) {
			sawStartingQuote = true;
			doubleQuote = true;
			value = new StringBuilder();
			return true;
		}
		
		if(optional) {
			value = null;
			return false;
		}
		
		sawStartingQuote = false;
		value = new StringBuilder().append(c);
		return true;
	}

	@Override
	public boolean next(char c) {
		if(sawEnd)
			return false; // Should never get here
		
		if(sawStartingQuote && ((doubleQuote && c == DOUBLE_QUOTE) || (!doubleQuote) && c == SINGLE_QUOTE)) {
			sawEnd = true;
			return true;
		}else if(!sawStartingQuote && Character.isWhitespace(c)) {
			sawEnd = true;
			return true;
		}
		
		value.append(c);
		return true;
	}
	
	@Override
	public boolean isTokenEnd() {
		return sawEnd;
	}

	@Override
	public boolean finish() {
		if(!sawStartingQuote) {
			return true; // it's possible we reached end of string instead of whitespace
		}
		
		return sawEnd;
	}

	@Override
	public FormattableObject toStandard() {
		return new GenericFormattableObject(value.toString());
	}

}
