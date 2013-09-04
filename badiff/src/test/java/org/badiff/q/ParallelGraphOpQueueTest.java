package org.badiff.q;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.badiff.Op;
import org.junit.Test;

public class ParallelGraphOpQueueTest {

	@Test
	public void testPerformance() throws Exception {
		final int SIZE = 2048 * 2048;
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
		q = new ParallelGraphOpQueue(q);
		
		long start = System.nanoTime();
		q.drainTo(new ArrayList<Op>());
		long end = System.nanoTime();
		
		System.out.println("Diffed " + SIZE + " bytes in " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms.");
	}

}
