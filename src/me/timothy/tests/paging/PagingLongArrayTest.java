package me.timothy.tests.paging;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Test;

import me.timothy.bots.paging.PagingLongArray;

public class PagingLongArrayTest {
	private PagingLongArray arr;
	
	@Test
	public void testUnsorted() {
		arr = new PagingLongArray(8);
		long[] real = new long[24];
		Random rand = new Random();
		
		for(int i = 0; i < 24; i++) {
			real[i] = rand.nextLong();
			arr.add(real[i]);
		}
		
		assertEquals(24, arr.size());
		
		for(int i = 0; i < 24; i++) {
			assertEquals(real[i], arr.get(i));
		}
		
		arr.dispose();
		arr = null;
	}
	
	@Test
	public void testSortLarge() {
		realTestSort(8000 * 16, 8000 * 64);
	}
	
	@Test
	public void testSortSmall() {
		realTestSort(8, 24);
	}
	
	private void realTestSort(int memorySize, int realSize) {
		arr = new PagingLongArray(memorySize);
		
		
		long[] real = new long[realSize];
		Random rand = new Random();
		
		for(int i = 0; i < realSize; i++) {
			real[i] = rand.nextInt(10000);
			arr.add(real[i]);
		}
		
		Arrays.sort(real);
		arr.sort();
		
		for(int i = 0; i < realSize; i++) {
			assertEquals(real[i], arr.get(i));
		}
		
		arr.dispose();
		arr = null;
	}
	
	@After
	public void cleanup() {
		if(arr != null) {
			arr.dispose();
			arr = null;
		}
	}
}
