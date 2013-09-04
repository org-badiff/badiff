package org.badiff;

import org.junit.Assert;
import org.junit.Test;

public class DiffTest {

	@Test
	public void testDiff() throws Exception {
		String orig = "Hello";
		String target = "World";
		
		BADiff diff = Diff.improved(Diff.queue(orig.getBytes(), target.getBytes()), 1024);
		
		byte[] result = Diff.applyDiff(diff, orig.getBytes());
		
		Assert.assertEquals(target, new String(result));
	}

}
