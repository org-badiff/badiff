package org.badiff.q;

import java.io.ByteArrayOutputStream;

import org.badiff.Diff;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.RandomInputStream;
import org.badiff.util.Serials;
import org.junit.Assert;
import org.junit.Test;

public class RewindingOpQueueTest {
	@Test
	public void test() throws Exception {
		String orig = "12345678901234567890abcdefghjiklmnopqrstuvwxyz";
		String target = "abcdefghjiklmnopqrstuvwxyz12345678901234567890";
		
		OpQueue q = new ReplaceOpQueue(orig.getBytes(), target.getBytes());
		q = new GraphOpQueue(q, Diff.DEFAULT_CHUNK);
		MemoryDiff md = new MemoryDiff(q);
		
		System.out.println(md + ": " + Serials.serialize(DefaultSerialization.newInstance(), MemoryDiff.class, md).length);
		
		
		q = md.queue();
		q = new RewindingOpQueue(q);
		
		MemoryDiff rwd = new MemoryDiff(q);
		
		System.out.println(rwd + ": " + Serials.serialize(DefaultSerialization.newInstance(), MemoryDiff.class, rwd).length);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		rwd.apply(new RandomInputStream(orig.getBytes()), bout);
		
		Assert.assertEquals(target, new String(bout.toByteArray()));
	}
}
