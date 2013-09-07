package org.badiff.nat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.badiff.Op;
import org.badiff.imp.FileDiff;
import org.badiff.q.BufferChunkingOpQueue;
import org.badiff.q.ChunkingOpQueue;
import org.badiff.q.CoalescingOpQueue;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.ParallelGraphOpQueue;
import org.badiff.q.ReplaceOpQueue;
import org.badiff.q.StreamChunkingOpQueue;
import org.badiff.util.Streams;
import org.junit.Assert;
import org.junit.Test;

public class NativeParallelGraphOpQueueTest {
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
		q = new NativeParallelGraphOpQueue(q);
		
		ByteArrayOutputStream result = new ByteArrayOutputStream(SIZE);
		
		long start = System.nanoTime();
		q.apply(
				new ByteArrayInputStream(orig.toByteArray()),
				result);
		long end = System.nanoTime();
		
		System.out.println("Diffed " + SIZE + " bytes in " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms.");
		
		byte[] tbuf = target.toByteArray();
		byte[] rbuf = result.toByteArray();
		
		q = new ReplaceOpQueue(tbuf, rbuf);
		q = new ChunkingOpQueue(q);
		q = new GraphOpQueue(q, 1024);
		q = new CoalescingOpQueue(q);
		List<Op> rdiff = q.drainTo(new ArrayList<Op>());
		System.out.println(rdiff);
		
		Assert.assertTrue(Arrays.equals(target.toByteArray(), result.toByteArray()));
	}
	
//	@Test
	public void testPerformanceBig() throws Exception {
		final int SIZE = 50 * 1024 * 1024;
		
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
		q = new NativeParallelGraphOpQueue(q);
		
//		FileDiff fd = new FileDiff(File.createTempFile("filediff", ".diff"));
		
		System.out.println("Starting diff");
		
		long start = System.nanoTime();
		
//		fd.store(q);
		q.drain();
		
		long end = System.nanoTime();
		
		System.out.println("Computed parallel graph diff for " + SIZE + " bytes in " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms");
		
		tin.close();
		oin.close();
		
//		fd.delete();
		orig.delete();
		target.delete();
	}
}
