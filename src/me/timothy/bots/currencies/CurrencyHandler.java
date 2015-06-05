package me.timothy.bots.currencies;       

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.timothy.bots.Retryable;
import me.timothy.jreddit.requests.Utils;

import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;

/**
 * Handles different currencies through http://www.freecurrencyconverterapi.com/api/v3/
 * 
 * @author Timothy
 */
public class CurrencyHandler {
	public static final String API_BASE = "http://www.freecurrencyconverterapi.com/api/v3/";
	
	public static final String CURRENCIES_LIST = API_BASE + "currencies";
	public static final String CONVERT = API_BASE + "convert";
	
	private static List<ConverterCurrency> currencies;
	/**
	 * Converts a symbol to a currency id. The id's are fetched from
	 * the server the first time this is used, and cached until the bot
	 * is restarted.
	 * 
	 * @param iso the iso 4217 to look up (e.g. EUR)
	 * @return the converter currency
	 */
	public static ConverterCurrency isoToCurrency(String iso) {
		if(currencies == null) {
			
			Retryable<JSONObject> currenciesRetryable = new Retryable<JSONObject>("Fetch currencies list") {

				@Override
				protected JSONObject runImpl() throws Exception {
					return (JSONObject) Utils.get(new URL(CURRENCIES_LIST), null);
				}
				
			};
			JSONObject jsonCurrencies = currenciesRetryable.run();
			
			JSONObject result = (JSONObject) jsonCurrencies.get("results");
			Set<?> keys = result.keySet();
			
			currencies = new ArrayList<>();
			for(Object key : keys) {
				currencies.add(new ConverterCurrency((JSONObject) result.get(key)));
			}
		}
		
		for(ConverterCurrency cur : currencies) {
			if(iso.equals(cur.iso4217))
				return cur;
		}
		return null;
	}
	
	/**
	 * Fetches the conversion rate from {@code from} to {@code to}
	 * @param from the currency to go from (e.g. EUR)
	 * @param to the currency to go to (e.g. USD)
	 * @return the conversion factor ( from * conversion_factor = to )
	 */
	public static double getConversionRate(String from, String to) {
		if(from.equals(to))
			return 1;
		
		String convId = from + "_" + to;
		final String apiParams = "q=" + convId + "&compact=ultra";
		
		Retryable<JSONObject> retryable = new Retryable<JSONObject>("Convert " + convId) {
			@Override
			protected JSONObject runImpl() throws Exception {
				return (JSONObject) Utils.get(apiParams, new URL(CONVERT), null);
			}
		};
		
		JSONObject result = retryable.run();
		if(result.keySet().size() == 0) {
			LogManager.getLogger().warn("Unknown conversion " + convId + ", assuming 1.00");
			return 1;
		}
		return ((Number) result.get(convId)).doubleValue();
	}
}
