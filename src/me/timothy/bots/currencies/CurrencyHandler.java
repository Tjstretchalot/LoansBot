package me.timothy.bots.currencies;       

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;

import me.timothy.bots.Retryable;
import me.timothy.jreddit.requests.Utils;

/**
 * Handles different currencies through apilayer
 * 
 * @author Timothy
 */
public class CurrencyHandler {
	public static final String API_BASE = "http://apilayer.net/api/live";
	private static CurrencyHandler instance;
	
	/**
	 * The access code used for connecting with the api layer.
	 */
	public String accessCode;
	
	static {
		instance = new CurrencyHandler();
	}
	
	/**
	 * Fetch the instance of the currency handler that should
	 * be used.
	 * 
	 * @return the instance of the currency handler
	 */
	public static CurrencyHandler getInstance() {
		return instance;
	}
	
	/**
	 * Fetches the conversion rate from {@code from} to {@code to}
	 * @param from the currency to go from (e.g. EUR)
	 * @param to the currency to go to (e.g. USD)
	 * @return the conversion factor ( from * conversion_factor = to )
	 */
	public double getConversionRate(String from, String to) {
		if(from.equals(to))
			return 1;
		if(from.length() != 3 || to.length() != 3)
			throw new IllegalArgumentException("Invalid currencies: from: " + from + ", to: " + to);
		String convId = from + "_" + to;
		final String apiParams = "access_key=" + accessCode + "&currencies=" + from + "&source=" + to + "&format=2";
		
		Retryable<JSONObject> retryable = new Retryable<JSONObject>("Convert " + convId) {
			private int times = 0;
			@Override
			protected JSONObject runImpl() throws Exception {
				times++;
				if(times > 3)
					return new JSONObject();
				return (JSONObject) Utils.get(apiParams, new URL(API_BASE), null, null, false);
			}
		};
		
		JSONObject result = retryable.run();
		boolean success = (Boolean) result.get("success");
		if(!success) {
			LogManager.getLogger().warn("Unknown conversion " + convId + ", assuming 1.00");
			return 1;
		}
		
		JSONObject quotes = (JSONObject) result.get("quotes");
		double toToFrom = ((Number) quotes.get(quotes.keySet().toArray()[0])).doubleValue();
		return 1 / toToFrom;
	}
}
