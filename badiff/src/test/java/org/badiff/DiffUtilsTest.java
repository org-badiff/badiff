package org.badiff;

import org.junit.Assert;
import org.junit.Test;

public class DiffUtilsTest {

	@Test
	public void testDiff() throws Exception {
		String orig = "Hello";
		String target = "World";
		
		Diff diff = DiffUtils.improved(DiffUtils.diff(orig.getBytes(), target.getBytes()), 1024);
		
		byte[] result = DiffUtils.applyDiff(diff, orig.getBytes());
		
		Assert.assertEquals(target, new String(result));
	}

}
