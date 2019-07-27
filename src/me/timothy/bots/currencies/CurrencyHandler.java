package me.timothy.bots.currencies;       

import java.net.URL;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.ws.http.HTTPException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import me.timothy.bots.Retryable;
import me.timothy.jreddit.requests.Utils;

/**
 * Handles different currencies through currencylayer aka apilayer
 * 
 * @author Timothy
 */
public class CurrencyHandler {
	public static final String API_BASE = "http://apilayer.net/api/live";
	private static CurrencyHandler instance;
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * The access code used for connecting with the api layer.
	 */
	public String accessCode;
	/**
	 * If not null, we have exceeded our subscription plan for the month and
	 * cannot convert currencies until the specified time.
	 */
	public Calendar exceededSubscriptionPlanUntil;
	
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
	
	private Calendar getQuotaResetsAt() {
		Calendar utc = Calendar.getInstance(TimeZone.getTimeZone(
				ZoneId.of("UTC").normalized()));
		
		Calendar next = Calendar.getInstance(TimeZone.getTimeZone(
				ZoneId.of("UTC").normalized()));
		next.clear();
		next.set(Calendar.YEAR, utc.get(Calendar.YEAR));
		next.set(Calendar.MONTH, utc.get(Calendar.MONTH));
		next.set(Calendar.DAY_OF_MONTH, 1);
		
		if(utc.get(Calendar.DAY_OF_MONTH) <= 3) {
			// This is likely just because currency layer is being a bit
			// slow in resetting the quota  / doesn't do so at exactly midnight
			next.set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH));
			next.add(Calendar.DAY_OF_MONTH, 1);
			return next;
		}
		
		next.add(Calendar.MONTH, 1);
		return next;
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
		if (exceededSubscriptionPlanUntil != null) {
			if (exceededSubscriptionPlanUntil.after(Calendar.getInstance(TimeZone.getTimeZone(
						ZoneId.of("UTC").normalized())))) {
					return 1;
			}
			exceededSubscriptionPlanUntil = null;
		}
		
		String convId = from + "_" + to;
		final String apiParams = "access_key=" + accessCode + "&currencies=" + from + "&source=" + to + "&format=2";
		
		Retryable<JSONObject> retryable = new Retryable<JSONObject>("Convert " + convId) {
			private int times = 0;
			@Override
			protected JSONObject runImpl() throws Exception {
				times++;
				if(times > 3)
					return new JSONObject();
				try {
					return (JSONObject) Utils.get(apiParams, new URL(API_BASE), null, null, false);
				}catch(HTTPException e) { 
					if(e.getStatusCode() == 104) {
						exceededSubscriptionPlanUntil = getQuotaResetsAt();
						logger.log(Level.WARN,
								"Exceeded CurrencyLayer monthly quota; no more conversions until %s",
								exceededSubscriptionPlanUntil.getTime());
						return new JSONObject();
					}else {
						throw e;
					}
				}
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
