package org.badiff.imp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.badiff.q.OpQueue;
import org.junit.Assert;
import org.junit.Test;

public class BadiffFileDiffTest {
	@Test
	public void testDiffWithRewind() throws Exception {
		File orig = File.createTempFile("orig", ".tmp");
		File target = File.createTempFile("target", ".tmp");
		
		FileUtils.write(orig, "12345678901234567890abcdefghjiklmnopqrstuvwxyz");
		FileUtils.write(target, "abcdefghjiklmnopqrstuvwxyz12345678901234567890");
		
		BadiffFileDiff diff = new BadiffFileDiff(File.createTempFile("diff", ".tmp"));
		diff.diff(orig, target);
		
		MemoryDiff md = new MemoryDiff(diff.queue());
		System.out.println(md);
		
		File applied = File.createTempFile("applied", ".tmp");
		diff.apply(orig, applied);
		
		try {
			Assert.assertEquals(FileUtils.readFileToString(target), FileUtils.readFileToString(applied));
			Assert.assertTrue(diff.header().getStats().getRewindCount() > 0);
		} finally {
			orig.delete();
			target.delete();
			diff.delete();
			applied.delete();
		}
	}
	@Test
	public void testDiffFromEmpty() throws Exception {
		File orig = File.createTempFile("orig", ".tmp");
		File target = File.createTempFile("target", ".tmp");
		
		FileUtils.write(orig, "");
		FileUtils.write(target, "abcdefghjiklmnopqrstuvwxyz12345678901234567890");
		
		BadiffFileDiff diff = new BadiffFileDiff(File.createTempFile("diff", ".tmp"));
		diff.diff(orig, target);
		
		MemoryDiff md = new MemoryDiff(diff.queue());
		System.out.println(md);
		
		File applied = File.createTempFile("applied", ".tmp");
		diff.apply(orig, applied);
		
		try {
			Assert.assertEquals(FileUtils.readFileToString(target), FileUtils.readFileToString(applied));
		} finally {
			orig.delete();
			target.delete();
			diff.delete();
			applied.delete();
		}
	}

	@Test
	public void testPerformance() throws Exception {
		final int SIZE = 2048 * 2048;
		
		ByteArrayOutputStream orig = new ByteArrayOutputStream(SIZE);
		ByteArrayOutputStream target = new ByteArrayOutputStream(SIZE);
		
		for(int i = 0; i < SIZE; i++) {
			orig.write((int)(256 * Math.random()));
			target.write((int)(256 * Math.random()));
		}
		
		BadiffFileDiff diff = new BadiffFileDiff(File.createTempFile("badiff", ".diff"));
		File origFile = File.createTempFile("orig", ".tmp");
		File targetFile = File.createTempFile("target", ".tmp");
		FileUtils.writeByteArrayToFile(origFile, orig.toByteArray());
		FileUtils.writeByteArrayToFile(targetFile, target.toByteArray());
		diff.diff(origFile, targetFile);
		
		OpQueue q = diff.queue();
		
		ByteArrayOutputStream result = new ByteArrayOutputStream(SIZE);
		
		q.apply(
				new DataInputStream(new ByteArrayInputStream(orig.toByteArray())),
				new DataOutputStream(result));
		
		Assert.assertTrue(Arrays.equals(target.toByteArray(), result.toByteArray()));
	}
}
