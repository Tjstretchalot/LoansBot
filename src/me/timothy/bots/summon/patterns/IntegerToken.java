package me.timothy.bots.summon.patterns;

import me.timothy.bots.responses.FormattableObject;
import me.timothy.bots.responses.GenericFormattableObject;

/**
 * Describes a token for a basic integer such as 1512
 * 
 * @author Timothy
 */
public class IntegerToken implements ISummonToken {
	private String id;
	private boolean optional;
	
	private StringBuilder value;
	
	public IntegerToken(String id, boolean optional) {
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
		if(!Character.isDigit(c))
			return false;
		value = new StringBuilder().append(c);
		return true;
	}

	@Override
	public boolean next(char c) {
		if(!Character.isDigit(c))
			return false;
		
		value.append(c);
		if(value.length() > 9) {
			long longVal = Long.valueOf(value.toString());
			if(longVal > Integer.MAX_VALUE)
				return false;
		}
		
		return true;
	}

	@Override
	public boolean finish() {
		return true;
	}

	@Override
	public FormattableObject toStandard() {
		return new GenericFormattableObject(value.toString());
	}
}
