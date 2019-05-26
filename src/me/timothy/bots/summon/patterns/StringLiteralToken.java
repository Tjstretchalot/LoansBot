package me.timothy.bots.summon.patterns;

import me.timothy.bots.responses.FormattableObject;

/**
 * This token is looking for a specific string literal.
 * 
 * @author Timothy
 */
public class StringLiteralToken implements ISummonToken {
	private String id;
	private boolean optional;
	private boolean caseInsensitive;
	
	private String literal;
	
	/**
	 * Where we are in literal currently. -1 for not started / failure
	 */
	private int currentIndex;
	
	/**
	 * Create a new string literal token that is attached to the
	 *  literal.
	 * 
	 * @param id the id for this token, used to determine if it was there
	 * @param literal the text that this token matches
	 * @param optional if this token is optional
	 * @param caseInsensitive true if this token is insensitive to case, false otherwise
	 */
	public StringLiteralToken(String id, String literal, boolean optional, boolean caseInsensitive) {
		this.id = id;
		this.literal = literal;
		this.optional = optional;
		this.caseInsensitive = caseInsensitive;
	}
	
	@Override
	public String id() {
		return id;
	}
	
	@Override
	public boolean isOptional() {
		return optional;
	}
	
	private boolean charMatch(char a, char b) {
		if(caseInsensitive)
			return Character.toLowerCase(a) == Character.toLowerCase(b);
		return a == b;
	}

	@Override
	public boolean start(char c) {
		currentIndex = -1;
		if(literal.length() == 0)
			return false;
		
		if(charMatch(c, literal.charAt(0))) {
			currentIndex = 1;
			return true;
		}
		return false;
	}

	@Override
	public boolean next(char c) {
		if(currentIndex < literal.length() && charMatch(literal.charAt(currentIndex), c)) {
			currentIndex++;
			return true;
		}
		
		currentIndex = -1;
		return false;
	}

	@Override
	public boolean finish() {
		return currentIndex == literal.length();
	}

	@Override
	public FormattableObject toStandard() {
		return null;
	}

}
