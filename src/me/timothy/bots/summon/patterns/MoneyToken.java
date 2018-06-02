package me.timothy.bots.summon.patterns;

import me.timothy.bots.responses.FormattableObject;
import me.timothy.bots.responses.MoneyFormattableObject;

/**
 * This token looks for money amounts which may optionally 
 * include dollar signs. These do not include a currency,
 * since currencies are universally handled by a 3-character
 * denomination on the next token (ex. 500 GDP)
 * 
 * This ensures that we only get 1 dot, that theres something
 * before the dot, everything except the dot is numbers, and that
 * there aren't more than 2 numbers after the dot, and produces
 * MoneyFormattableObjects.
 * 
 * @author Timothy
 */
public class MoneyToken implements ISummonToken {
	private String id;
	private boolean optional;
	
	private StringBuilder beforeDot;
	private boolean sawDot;
	private StringBuilder afterDot;
	private boolean sawTrailingDollarSign;
	
	/**
	 * We will allow commas to be included, but if
	 * we see them we will enforce them.
	 */
	private int digitsSinceLastComma;
	private boolean sawComma;
	
	/**
	 * Create a new money token attached to the given id
	 * 
	 * @param id how to reference this token
	 * @param optional if this token is optional
	 */
	public MoneyToken(String id, boolean optional) {
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
		beforeDot = null;
		sawDot = false;
		afterDot = null;
		sawTrailingDollarSign = false;
		digitsSinceLastComma = 0;
		sawComma = false;
		
		
		if(c == '$') {
			return true;
		}else if(Character.isDigit(c))
		{
			if(c == '0')
				return false;
			
			beforeDot = new StringBuilder().append(c);
			digitsSinceLastComma = 1;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean next(char c) {
		if(sawTrailingDollarSign)
			return false;
		
		if(c == '$') {
			if(beforeDot == null)
				return false;
			if(sawDot && afterDot != null && afterDot.length() == 1)
				return false; // disallow $450.5
			if(sawComma && digitsSinceLastComma != 3)
				return false; // disallow 13,50$ 
			
			sawTrailingDollarSign = true;
			return true;
		}
		
		if(!sawDot) {
			if(Character.isDigit(c)) {
				if(beforeDot == null) {
					if(c == '0')
						return false;
					
					beforeDot = new StringBuilder();
				}
				beforeDot.append(c);
				digitsSinceLastComma++;
				
				return true;
			}else if(c == '.') {
				sawDot = true;
				return true;
			}else if(c == ',') {
				if(!sawComma) {
					sawComma = true;
					digitsSinceLastComma = 0;
					return true;
				}else if(digitsSinceLastComma == 3) {
					digitsSinceLastComma = 0;
					return true;
				}
				
				return false;
			}
			
			return false;
		}else {
			if(Character.isDigit(c)) {
				if(afterDot == null)
					afterDot = new StringBuilder();
				else if(afterDot.length() == 2)
					return false;
				
				afterDot.append(c);
				
				return true;
			}
			
			return false;
		}
	}

	@Override
	public boolean finish() {
		if(beforeDot == null)
			return false;
		if(sawComma && digitsSinceLastComma != 3)
			return false;
		
		if(sawDot && afterDot != null && afterDot.length() == 1)
			return false; // disallow $5.5
		
		return true;
	}

	@Override
	public FormattableObject toStandard() {
		int cents = Integer.parseInt(beforeDot.toString()) * 100;
		if(afterDot != null) {
			cents += Integer.parseInt(afterDot.toString());
		}
		
		beforeDot = null;
		afterDot = null;
		
		return new MoneyFormattableObject(cents);
	}
}
