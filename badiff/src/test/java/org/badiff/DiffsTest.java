package org.badiff;

import org.badiff.q.OpQueue;
import org.badiff.util.Diffs;
import org.junit.Assert;
import org.junit.Test;

public class DiffsTest {

	@Test
	public void testDiff() throws Exception {
		String orig = "Hello";
		String target = "World";
		
		OpQueue diff = Diffs.improved(Diffs.queue(orig.getBytes(), target.getBytes()));
		
		byte[] result = Diffs.apply(diff, orig.getBytes());
		
		Assert.assertEquals(target, new String(result));
	}

}
