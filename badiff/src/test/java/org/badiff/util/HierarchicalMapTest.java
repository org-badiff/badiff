package org.badiff.util;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HierarchicalMapTest {
	private static Map<Integer, Integer> map(int[][] pairs) {
		Map<Integer, Integer> m = new TreeMap<Integer, Integer>();
		for(int[] pair : pairs)
			m.put(pair[0], pair[1]);
		return m;
	}
	
	private Map<Integer, Integer> parent;
	private Map<Integer, Integer> child;
	private Map<Integer, Integer> hier;
	private Map<Integer, Integer> all;
	

	@Before
	public void setUp() throws Exception {
		child = map(new int[][] { {1,1}, {2,2}, {3,3}, {4,4}, {5,5} });
		parent = map(new int[][] { {4,5}, {5,6}, {6,7}, {7,8}, {8,9} });
		hier = new HierarchicalMap<Integer, Integer>(child, parent);
		all = new TreeMap<Integer, Integer>(parent);
		all.putAll(child);
	}

	@Test
	public void testSize() {
		Assert.assertEquals(all.size(), hier.size());
		hier.clear();
		Assert.assertEquals(parent.size(), hier.size());
		parent.clear();
		Assert.assertEquals(0, hier.size());
	}

	@Test
	public void testIsEmpty() {
		Assert.assertFalse(hier.isEmpty());
		hier.clear();
		Assert.assertFalse(hier.isEmpty());
		parent.clear();
		Assert.assertTrue(hier.isEmpty());
		hier.put(1, 1);
		Assert.assertFalse(hier.isEmpty());
	}

	@Test
	public void testEntrySet() {
		Assert.assertEquals(all.entrySet(), hier.entrySet());
		parent.put(4, 4);
		hier.put(4, 5);
		Assert.assertNotEquals(all.entrySet(), hier.entrySet());
		hier.remove(4);
		Assert.assertEquals(all.entrySet(), hier.entrySet());
	}

	@Test
	public void testGetObject() {
		Assert.assertEquals(1, (int) hier.get(1));
		Assert.assertEquals(7, (int) hier.get(6));
		Assert.assertEquals(4, (int) hier.get(4));
		hier.remove(4);
		Assert.assertEquals(5, (int) hier.get(4));
		parent.remove(6);
		Assert.assertNull(hier.get(6));
	}

	@Test
	public void testPutKV() {
		hier.put(1, 2);
		Assert.assertEquals(2, (int) hier.get(1));
		Assert.assertEquals(7, (int) hier.get(6));
		hier.put(6, 6);
		Assert.assertEquals(6, (int) hier.get(6));
	}

	@Test
	public void testRemoveObject() {
		Assert.assertEquals(1, (int) hier.remove(1));
		Assert.assertNull(hier.get(1));
		Assert.assertEquals(5, (int) hier.remove(5));
		Assert.assertEquals(6, (int) hier.get(5));
		Assert.assertNull(hier.remove(5));
		Assert.assertEquals(6, (int) hier.get(5));
	}

	@Test
	public void testContainsKeyObject() {
		Assert.assertTrue(hier.containsKey(1));
		Assert.assertTrue(hier.containsKey(4));
		Assert.assertTrue(hier.containsKey(7));
		hier.remove(1);
		hier.remove(4);
		hier.remove(7);
		Assert.assertFalse(hier.containsKey(1));
		Assert.assertTrue(hier.containsKey(4));
		Assert.assertTrue(hier.containsKey(7));
	}

	@Test
	public void testKeySet() {
		Assert.assertEquals(all.keySet(), hier.keySet());
		hier.keySet().clear();
		Assert.assertEquals(parent.keySet(), hier.keySet());
		hier.put(1, 1);
		Assert.assertTrue(hier.containsKey(1));
	}

	@Test
	public void testClear() {
		hier.clear();
		Assert.assertEquals(parent, hier);
		parent.clear();
		Assert.assertEquals(Collections.emptyMap(), hier);
	}

	@Test
	public void testContainsValue() {
		System.out.println(hier);
		System.out.println(((HierarchicalMap<Integer, Integer>) hier).keys);
		Assert.assertFalse(hier.containsValue(6));
		hier.remove(5);
		System.out.println(hier);
		System.out.println(((HierarchicalMap<Integer, Integer>) hier).keys);
		Assert.assertTrue(hier.containsValue(6));
	}

}
