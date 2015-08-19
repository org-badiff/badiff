package org.badiff.util;

import org.junit.Assert;
import org.junit.Test;

public class Integers2Test {
	@Test
	public void testNegative() {
		Assert.assertEquals(0L, Integers.negative(1L));
		Assert.assertEquals(0L, Integers.negative(0L));
		Assert.assertEquals(-1L, Integers.negative(-1L));

		Assert.assertEquals(0, Integers.negative(1));
		Assert.assertEquals(0, Integers.negative(0));
		Assert.assertEquals(-1, Integers.negative(-1));
	}
	
	@Test
	public void testMin() {
		Assert.assertEquals(-1L, Integers.min(-1L, -1L));
		Assert.assertEquals(-1L, Integers.min(-1L, 0L));
		Assert.assertEquals(-1L, Integers.min(-1L, 1L));
		Assert.assertEquals(-1L, Integers.min(0L, -1L));
		Assert.assertEquals(0L, Integers.min(0L, 0L));
		Assert.assertEquals(0L, Integers.min(0L, 1L));
		Assert.assertEquals(-1L, Integers.min(1L, -1L));
		Assert.assertEquals(0L, Integers.min(1L, 0L));
		Assert.assertEquals(1L, Integers.min(1L, 1L));

		Assert.assertEquals(-1, Integers.min(-1, -1));
		Assert.assertEquals(-1, Integers.min(-1, 0));
		Assert.assertEquals(-1, Integers.min(-1, 1));
		Assert.assertEquals(-1, Integers.min(0, -1));
		Assert.assertEquals(0, Integers.min(0, 0));
		Assert.assertEquals(0, Integers.min(0, 1));
		Assert.assertEquals(-1, Integers.min(1, -1));
		Assert.assertEquals(0, Integers.min(1, 0));
		Assert.assertEquals(1, Integers.min(1, 1));
	}
	
	@Test
	public void testMax() {
		Assert.assertEquals(-1L, Integers.max(-1L, -1L));
		Assert.assertEquals(0L, Integers.max(-1L, 0L));
		Assert.assertEquals(1L, Integers.max(-1L, 1L));
		Assert.assertEquals(0L, Integers.max(0L, -1L));
		Assert.assertEquals(0L, Integers.max(0L, 0L));
		Assert.assertEquals(1L, Integers.max(0L, 1L));
		Assert.assertEquals(1L, Integers.max(1L, -1L));
		Assert.assertEquals(1L, Integers.max(1L, 0L));
		Assert.assertEquals(1L, Integers.max(1L, 1L));

		Assert.assertEquals(-1, Integers.max(-1, -1));
		Assert.assertEquals(0, Integers.max(-1, 0));
		Assert.assertEquals(1, Integers.max(-1, 1));
		Assert.assertEquals(0, Integers.max(0, -1));
		Assert.assertEquals(0, Integers.max(0, 0));
		Assert.assertEquals(1, Integers.max(0, 1));
		Assert.assertEquals(1, Integers.max(1, -1));
		Assert.assertEquals(1, Integers.max(1, 0));
		Assert.assertEquals(1, Integers.max(1, 1));
	}
}
