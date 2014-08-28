package me.timothy.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An extremely simple map implementation that doesn't
 * require both parameters to have valid hashcode/equals. Simply 
 * 2 lists that that have the key/value at the same index in each other.
 * 
 * 
 * @author Timothy
 *
 * @param <T1> keys
 * @param <T2> values
 */
public class BasicMap<T1, T2> {
	private List<T1> allType1;
	private List<T1> unmodifiableType1;
	private List<T2> allType2;
	
	public BasicMap() {
		allType1 = new ArrayList<>();
		allType2 = new ArrayList<>();
	}
	
	public List<T1> getKeys() {
		if(unmodifiableType1 != null)
			return unmodifiableType1;
		return unmodifiableType1 = Collections.unmodifiableList(allType1);
	}
	
	public T2 get(T1 key) {
		return allType2.get(allType1.indexOf(key));
	}
	
	public void put(T1 key, T2 value) {
		allType1.add(key);
		allType2.add(value);
	}
	
	public boolean containsKey(T1 key) {
		return getKeys().contains(key);
	}

	public void remove(T1 c) {
		int index = allType1.indexOf(c);
		
		allType1.remove(index);
		allType2.remove(index);
		
		unmodifiableType1 = null;
	}
}
