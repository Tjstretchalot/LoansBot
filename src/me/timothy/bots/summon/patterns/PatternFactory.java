package me.timothy.bots.summon.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * This factory builds SummonPatterns. Build only works once.
 * 
 * @author Timothy
 */
public class PatternFactory {
	private List<ISummonToken> tokens;
	
	/**
	 * Create a new pattern factory
	 */
	public PatternFactory() {
		tokens = new ArrayList<ISummonToken>();
	}
	
	/**
	 * Require or allow that the next token is a string literal
	 * 
	 * @param id the id to check if this was there, null is allowed but won't know if there
	 * @param literal the string literal you want
	 * @Param optional if the literal is optional. you will need to check the id to know if it was there
	 * @return this
	 */
	public PatternFactory addLiteral(String id, String literal, boolean optional) {
		tokens.add(new StringLiteralToken(id, literal, optional));
		return this;
	}
	
	/**
	 * Add a non-optional literal without an id.
	 * 
	 * @param literal the literal to add
	 * @return this
	 */
	public PatternFactory addLiteral(String literal) {
		return addLiteral(null, literal, false);
	}
	
	/**
	 * Require or allow that the next token be a username reference
	 * @param id how to fetch this username later
	 * @return this
	 */
	public PatternFactory addUsername(String id, boolean optional) {
		tokens.add(new UsernameToken(id, optional));
		return this;
	}
	
	/**
	 * Require that the next token be a username reference
	 * @param id
	 * @return
	 */
	public PatternFactory addUsername(String id) {
		return addUsername(id, false);
	}
	
	/**
	 * Require or allow that the next token be a maximally-2-digit-precise
	 * number that will be converted into a MoneyFormattableObject
	 * without any rounding errors
	 * 
	 * @param id how to fetch this money later
	 * @return this
	 */
	public PatternFactory addMoney(String id, boolean optional) {
		tokens.add(new MoneyToken(id, optional));
		return this;
	}
	
	/**
	 * Require that the next token be convertable to a MoneyFormattableObject
	 * 
	 * @param id the id to reference the money in this token
	 * @return this
	 */
	public PatternFactory addMoney(String id) {
		return addMoney(id, false);
	}
	
	/**
	 * Require or allow that the next token be a currency deliminator, eg USD or GDP
	 * 
	 * @param id how to reference this currency later
	 * @param optional true if this token may be omitted, false otherwise
	 * @return this
	 */
	public PatternFactory addCurrency(String id, boolean optional) {
		tokens.add(new CurrencyToken(id, optional));
		return this;
	}
	
	/**
	 * Require that the next token be a currency deliminator 
	 * 
	 * @param id how to reference this currency later
	 * @return this
	 */
	public PatternFactory addCurrency(String id) {
		return addCurrency(id, false);
	}
	
	/**
	 * Require that the next token be a quoted string (if optional, quotes are
	 * required and indicate it is this token, otherwise quotes may be omitted
	 * when there is no whitespace)
	 * 
	 * @param id how to reference this token later
	 * @param optional true if this token may be omitted, false otherwise
	 * @return this
	 */
	public PatternFactory addQuotedString(String id, boolean optional) {
		tokens.add(new QuotedStringToken(id, optional));
		return this;
	}
	
	/**
	 * Require that the next token be a quoted string or a single word which
	 * can be referenced through id
	 * 
	 * @param id how to reference this token later
	 * @return this
	 */
	public PatternFactory addQuotedString(String id) {
		return addQuotedString(id, false);
	}
	
	/**
	 * Build the summon pattern. After calling this all functions will
	 * throw an NPE
	 * 
	 * @return the built pattern
	 */
	public SummonPattern build() {
		if(tokens == null)
			throw new NullPointerException();
		
		List<ISummonToken> _tokens = tokens;
		tokens = null;
		return new SummonPattern(_tokens);
	}
}
