package me.timothy.bots.currencies;

import org.json.simple.JSONObject;

public class ConverterCurrency {
	public final String symbol;
	public final String iso4217;
	public final String name;
	
	/**
	 * Create a converter currency
	 * @param symbol the symbol (e.g. €)
	 * @param iso4217 the iso 4217 name (e.g. EUR)
	 * @param name the longer name (e.g. European Euro)
	 */
	public ConverterCurrency(String symbol, String iso4217, String name) {
		this.symbol = symbol;
		this.iso4217 = iso4217;
		this.name = name;
	}
	
	/**
	 * Parses the specified object that is a response from the converter
	 * api
	 * 
	 * @param object the object to parse
	 */
	public ConverterCurrency(JSONObject object) {
		this.symbol = (String) object.get("currencySymbol");
		this.iso4217 = (String) object.get("id");
		this.name = (String) object.get("currencyName");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConverterCurrency [symbol=" + symbol + ", iso4217=" + iso4217 + ", name=" + name + "]";
	}
	
	
}
