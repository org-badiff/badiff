package org.badiff.q;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.badiff.imp.FileDiff;
import org.badiff.util.Streams;
import org.junit.Assert;
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
		
		ByteArrayOutputStream result = new ByteArrayOutputStream(SIZE);
		
		long start = System.nanoTime();
		q.apply(
				new ByteArrayInputStream(orig.toByteArray()),
				result);
		long end = System.nanoTime();
		
		System.out.println("Diffed " + SIZE + " bytes in " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms.");
		
		Assert.assertTrue(Arrays.equals(target.toByteArray(), result.toByteArray()));
	}
	
//	@Test
	public void testPerformanceBig() throws Exception {
		final int SIZE = 24 * 1024 * 1024;
		
		File orig = File.createTempFile("orig", ".tmp");
		orig.deleteOnExit();
		
		File target = File.createTempFile("target", ".tmp");
		target.deleteOnExit();
		
		InputStream random = new FileInputStream("/dev/urandom");
		
		OutputStream out;
		
		System.out.println("Creating random input files");
		
		Streams.copy(random, out = new FileOutputStream(orig), SIZE); out.close();
		Streams.copy(random, out = new FileOutputStream(target), SIZE); out.close();
		
		random.close();
		
		System.out.println("Mapping input to RAM...");
		
		FileInputStream oin = new FileInputStream(orig);
		FileInputStream tin = new FileInputStream(target);
		
		MappedByteBuffer obuf = oin.getChannel().map(MapMode.READ_ONLY, 0, orig.length());
		MappedByteBuffer tbuf = oin.getChannel().map(MapMode.READ_ONLY, 0, target.length());
		
		obuf.load();
		tbuf.load();
		
		OpQueue q = new BufferChunkingOpQueue(obuf, tbuf);
		q = new ParallelGraphOpQueue(q);
		
		FileDiff fd = new FileDiff(File.createTempFile("filediff", ".diff"));
		
		System.out.println("Starting diff");
		
		long start = System.nanoTime();
		
		fd.store(q);
		
		long end = System.nanoTime();
		
		System.out.println("Computed FileDiff for " + SIZE + " bytes in " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms");
		
		tin.close();
		oin.close();
		
		fd.delete();
		orig.delete();
		target.delete();
	}

}
