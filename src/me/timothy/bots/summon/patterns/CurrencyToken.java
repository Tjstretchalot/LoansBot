package me.timothy.bots.summon.patterns;

import me.timothy.bots.responses.FormattableObject;
import me.timothy.bots.responses.GenericFormattableObject;

/**
 * This token looks for a 3-character all-alphabetic all-uppercase string
 * 
 * @author Timothy
 *
 */
public class CurrencyToken implements ISummonToken {
	private String id;
	private boolean optional;
	private StringBuilder parsed;
	
	/**
	 * Create a new currency token referencable by the given id.
	 * 
	 * @param id the id, ex currency
	 * @param optional if this token is optional
	 */
	public CurrencyToken(String id, boolean optional) {
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
		if(!Character.isAlphabetic(c) || !Character.isUpperCase(c))
			return false;
		
		parsed = new StringBuilder().append(c);
		
		return true;
	}

	@Override
	public boolean next(char c) {
		if(parsed.length() == 3)
			return false;
		
		if(!Character.isAlphabetic(c) || !Character.isUpperCase(c))
			return false;
		
		parsed.append(c);
		return true;
	}

	@Override
	public boolean finish() {
		if(parsed.length() != 3)
			return false;
		
		return true;
	}

	@Override
	public FormattableObject toStandard() {
		GenericFormattableObject result = new GenericFormattableObject(parsed.toString());
		parsed = null;
		return result;
	}

}
