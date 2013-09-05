package org.badiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.badiff.imp.FileDiff;
import org.badiff.util.Streams;
import org.junit.Test;

public class FileDiffsTest {
	@Test
	public void testPerformanceBig() throws Exception {
		final int SIZE = 50 * 1024 * 1024;
		
		File orig = File.createTempFile("orig", ".tmp");
		orig.deleteOnExit();
		
		File target = File.createTempFile("target", ".tmp");
		target.deleteOnExit();
		
		InputStream random = new FileInputStream("/dev/urandom");
		
		OutputStream out;
		
		Streams.copy(random, out = new FileOutputStream(orig), SIZE); out.close();
		Streams.copy(random, out = new FileOutputStream(target), SIZE); out.close();
		
		random.close();
		
		FileDiffs util = new FileDiffs();
		
		long start = System.nanoTime();
		FileDiff fd = util.mdiff(orig, target);
		long end = System.nanoTime();
		
		System.out.println("Computed FileDiff for " + SIZE + " bytes in " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms");
		
		fd.delete();
		orig.delete();
		target.delete();
	}

}
