package org.badiff.imp;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
}
