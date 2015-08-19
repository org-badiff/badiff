package org.badiff.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IntegersTest {
	@Parameters
	public static Iterable<Object[]> params() {
		List<Object[]> p = new ArrayList<Object[]>();
		p.add(new Object[] {0L});
		for(long i = 1; i != 0; i = i << 1)
			p.add(new Object[] {i});
		return p;
	}
	
	private long i;
	
	public IntegersTest(Long i) {
		this.i = i;
	}
	
	@Test
	public void testAny() {
		Assert.assertEquals(i == 0 ? 0 : -1, Integers.any(i));
		Assert.assertEquals(((int) i) == 0 ? 0 : -1, Integers.any((int) i));
		Assert.assertEquals(((short) i) == 0 ? 0 : -1, Integers.any((short) i));
	}
	
	@Test
	public void testAll() {
		long i = ~this.i;
		Assert.assertEquals(i == -1 ? -1 : 0, Integers.all(i));
		Assert.assertEquals(((int) i) == -1 ? -1 : 0, Integers.all((int) i));
		Assert.assertEquals(((short) i) == -1 ? -1 : 0, Integers.all((short) i));
	}
}
