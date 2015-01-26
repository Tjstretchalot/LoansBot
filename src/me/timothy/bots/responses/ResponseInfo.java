package me.timothy.bots.responses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows sophisticated, but easy, formatting for responses by 
 * combining long-term things like headers, footers, etc. with 
 * information pertinent to a specific response
 * 
 * @author Timothy
 *
 */
public class ResponseInfo {
	/**
	 * Map of all the objects, permanent and temporary
	 * alike.
	 */
	private Map<String, FormattableObject> map;
	
	/**
	 * The list of keys that should be deleted prior to formatting
	 * the next request
	 */
	private List<String> tempKeys;
	
	/**
	 * Creates a new response info with an empty map
	 */
	public ResponseInfo() {
		map = new HashMap<>();
		tempKeys = new ArrayList<>();
	}
	
	/**
	 * Creates a response info that shallow copies 
	 * another response info
	 * @param o the object to shallow copy
	 */
	public ResponseInfo(ResponseInfo o) {
		this();
		
		Set<String> keys = o.map.keySet();
		
		for(String key : keys) {
			map.put(key, o.map.get(key));
		}
		
		tempKeys.addAll(o.tempKeys);
	}
	/**
	 * Adds a longterm object to this response info
	 * 
	 * @param key the key to use
	 * @param obj the object mapped to the key
	 */
	public void addLongtermObject(String key, FormattableObject obj) {
		map.put(key, obj);
	}
	
	/**
	 * Adds a temporary object to the response info
	 * 
	 * @param key the key to use
	 * @param obj the object mapped to key
	 */
	public void addTemporaryObject(String key, FormattableObject obj) {
		map.put(key, obj);
		tempKeys.add(key);
	}
	
	/**
	 * Gets an object, either permanent or temporary
	 * 
	 * @param key the key to search for
	 * @return the mapped object or null
	 */
	public FormattableObject getObject(String key) {
		return map.get(key);
	}
	
	/**
	 * Clears all the temporary objects
	 */
	public void clearTemporary() {
		for(String tKey : tempKeys) {
			map.remove(tKey);
		}
		tempKeys.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("{");
		boolean first = true;
		
		Set<String> keys = map.keySet();
		for(String key : keys) {
			if(first)
				first = false;
			else
				result.append(", ");
			boolean temp = tempKeys.contains(key);
			
			result.append("[").append(key).append(": ").append(map.get(key));
			if(temp)
				result.append(" TEMP");
			
			result.append("]");
		}
		
		result.append("}");
		return result.toString();
	}
}
