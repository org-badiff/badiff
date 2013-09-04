package org.badiff;

import org.badiff.q.OpQueue;
import org.junit.Assert;
import org.junit.Test;

public class DiffUtilsTest {

	@Test
	public void testDiff() throws Exception {
		String orig = "Hello";
		String target = "World";
		
		OpQueue diff = DiffUtils.improved(DiffUtils.queue(orig.getBytes(), target.getBytes()));
		
		byte[] result = DiffUtils.apply(diff, orig.getBytes());
		
		Assert.assertEquals(target, new String(result));
	}

}
