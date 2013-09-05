package org.badiff;

import org.badiff.imp.MemoryDiff;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.util.Diffs;
import org.junit.Assert;
import org.junit.Test;

public class DiffsTest {

	@Test
	public void testDiff() throws Exception {
		String orig = "Hello world!";
		String target = "Hellish cruel world!";
		
		OpQueue diff = Diffs.queue(orig.getBytes(), target.getBytes());
		diff = new GraphOpQueue(diff, 1024);
		
		MemoryDiff md = new MemoryDiff(diff);
		
		byte[] result = Diffs.apply(md, orig.getBytes());
		
		
		Assert.assertEquals(target, new String(result));
	}

}
