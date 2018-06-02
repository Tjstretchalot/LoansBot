package me.timothy.bots.summon.patterns;

import me.timothy.bots.responses.FormattableObject;

/**
 * Describes a token in the summon pattern. Tokens take in 
 * a sequence of characters and parse them into a standard
 * string format, which can then be sent off to a different
 * parser for meaning.
 * 
 * @author Timothy
 */
public interface ISummonToken {
	/**
	 * The unique identifier for this token. This is used to fetch the 
	 * formattable object from a dictionary after it has been parsed.
	 * 
	 * @return the id for this token.
	 */
	public String id();
	
	/**
	 * If this token is optional. Optional tokens will be checked for their start in the order
	 * they were added by the factory.
	 * 
	 * @return if this token is optional
	 */
	public boolean isOptional();
	
	/**
	 * Indicates this token should begin processing for a new string
	 * starting with the given character.
	 * 
	 * @param c the first character to process
	 * @return true if processing for this token can continue, false on failure
	 */
	public boolean start(char c);
	
	/**
	 * Indicates this token should process the next character.
	 * 
	 * @param c the next character to process
	 * @return true if processing for this token can continue, false on failure
	 */
	public boolean next(char c);
	
	/**
	 * Indicates this token should try to process to standard with what it has now.
	 * 
	 * @return true if the token is now valid, false otherwise
	 */
	public boolean finish();
	
	/**
	 * After a true to canFinish, this should return, on the next call, the 
	 * representation of this token in standard format. If the last call was not
	 * canFinish and the result was not true, this function may return anything.
	 * 
	 * May be null if this token does not correspond to a formattable object.
	 * 
	 * @return the standard representation of the just parsed token.
	 */
	public FormattableObject toStandard();
}
