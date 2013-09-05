package org.badiff.q;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.badiff.Op;
import org.junit.Assert;
import org.junit.Test;

public class GraphOpQueueTest {

	@Test
	public void testPerformance() throws Exception {
		final int SIZE = 1024 * 1024;
		final int CHUNK = 1024;
		
		ByteArrayOutputStream orig = new ByteArrayOutputStream(SIZE);
		ByteArrayOutputStream target = new ByteArrayOutputStream(SIZE);
		
		for(int i = 0; i < SIZE; i++) {
			orig.write((int)(256 * Math.random()));
			target.write((int)(256 * Math.random()));
		}
		
		OpQueue q = new StreamChunkingOpQueue(
				new ByteArrayInputStream(orig.toByteArray()), 
				new ByteArrayInputStream(target.toByteArray()), 
				CHUNK);
		q = new GraphOpQueue(q, CHUNK);
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		long start = System.nanoTime();
		q.apply(
				new ByteArrayInputStream(orig.toByteArray()),
				result);
		long end = System.nanoTime();
		
		System.out.println("Diffed " + SIZE + " bytes in " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms.");

/*
		byte[] tbuf = target.toByteArray();
		byte[] rbuf = result.toByteArray();
		
		q = new ReplaceOpQueue(tbuf, rbuf);
		q = new ChunkingOpQueue(q);
		q = new GraphOpQueue(q, 1024);
		q = new CoalescingOpQueue(q);
		List<Op> rdiff = q.drainTo(new ArrayList<Op>());
		System.out.println(rdiff);
*/		
		Assert.assertTrue(Arrays.equals(target.toByteArray(), result.toByteArray()));
	}

}
