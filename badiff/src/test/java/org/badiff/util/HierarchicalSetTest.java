package org.badiff.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HierarchicalSetTest {
	
	private Set<Integer> parent;
	private Set<Integer> child;
	private Set<Integer> hier;
	private Set<Integer> all;

	@Before
	public void setUp() throws Exception {
		child = new TreeSet<Integer>(Arrays.asList(1,2,3,4,5));
		parent = new TreeSet<Integer>(Arrays.asList(4,5,6,7,8));
		hier = new HierarchicalSet<Integer>(child, parent);
		all = new TreeSet<Integer>(parent);
		all.addAll(child);
	}

	@Test
	public void testSize() {
		Assert.assertEquals(all.size(), hier.size());
	}

	@Test
	public void testIsEmpty() {
		Assert.assertFalse(hier.isEmpty());
		hier.clear();
		Assert.assertFalse(hier.isEmpty());
		parent.clear();
		Assert.assertTrue(hier.isEmpty());
		hier.add(1);
		Assert.assertFalse(hier.isEmpty());
	}

	@Test
	public void testClear() {
		hier.clear();
		Assert.assertEquals(parent, hier);
	}

	@Test
	public void testIterator() {
		Assert.assertEquals(Arrays.asList(1,2,3,4,5,6,7,8), new ArrayList<Integer>(hier));
		Iterator<Integer> hi = hier.iterator();
		while(hi.next() != 4)
			;
		hi.remove();
		Assert.assertEquals(5, (int) hi.next());
		hi.remove();
		Assert.assertEquals(4, (int) hi.next());
		try {
			hi.remove();
			Assert.fail();
		} catch(UnsupportedOperationException expected) {
		}
		Assert.assertEquals(Arrays.asList(1,2,3,4,5,6,7,8), new ArrayList<Integer>(hier));
		
	}

	@Test
	public void testRemoveAllCollectionOfQ() {
		Assert.assertFalse(hier.removeAll(parent));
		Assert.assertEquals(all, hier);
		all.removeAll(parent);
		parent.clear();
		Assert.assertEquals(all, hier);
	}

	@Test
	public void testContainsObject() {
		Assert.assertTrue(hier.contains(1));
		hier.remove(1);
		Assert.assertFalse(hier.contains(1));
		hier.remove(4);
		Assert.assertTrue(hier.contains(4));
		parent.remove(4);
		Assert.assertFalse(hier.contains(4));
	}

	@Test
	public void testAddE() {
		Assert.assertFalse(hier.add(1));
		Assert.assertFalse(hier.add(8));
		Assert.assertTrue(hier.remove(1));
		Assert.assertFalse(hier.contains(1));
		Assert.assertFalse(hier.remove(8));
		parent.remove(8);
		Assert.assertTrue(hier.add(8));
	}

	@Test
	public void testRemoveObject() {
		Assert.assertTrue(hier.remove(1));
		Assert.assertFalse(hier.remove(5));
		Assert.assertFalse(hier.remove(5));
	}

	@Test
	public void testContainsAllCollectionOfQ() {
		Assert.assertEquals(all, hier);
		hier.remove(4);
		hier.remove(5);
		Assert.assertEquals(all, hier);
	}

	@Test
	public void testAddAllCollectionOfQextendsE() {
		Assert.assertFalse(hier.addAll(Arrays.asList(8)));
		Assert.assertTrue(hier.addAll(Arrays.asList(0,6)));
		parent.clear();
		Set<Integer> exp = new TreeSet<Integer>(Arrays.asList(0,1,2,3,4,5,6,8));
		Assert.assertEquals(exp, hier);
	}

	@Test
	public void testRetainAllCollectionOfQ() {
		Assert.assertFalse(hier.retainAll(child));
		Assert.assertTrue(hier.retainAll(parent));
		Assert.assertEquals(parent, hier);
		parent.clear();
		Assert.assertEquals(new TreeSet<Integer>(Arrays.asList(4,5)), hier);
	}

}
