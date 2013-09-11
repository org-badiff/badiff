package org.badiff.q;

import org.badiff.Diff;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.util.Serials;
import org.junit.Test;

public class RewindingOpQueueTest {
	@Test
	public void test() throws Exception {
		String orig = "12345678901234567890abcdefghjiklmnopqrstuvwxyz";
		String target = "abcdefghjiklmnopqrstuvwxyz12345678901234567890";
		
		OpQueue q = new ReplaceOpQueue(orig.getBytes(), target.getBytes());
		q = new GraphOpQueue(q, Diff.DEFAULT_CHUNK);
		MemoryDiff md = new MemoryDiff(q);
		
		System.out.println(md + ": " + Serials.serialize(DefaultSerialization.getInstance(), MemoryDiff.class, md).length);
		
		
		q = md.queue();
		q = new RewindingOpQueue(q);
		
		MemoryDiff rwd = new MemoryDiff(q);
		
		System.out.println(rwd + ": " + Serials.serialize(DefaultSerialization.getInstance(), MemoryDiff.class, rwd).length);
	}
}
