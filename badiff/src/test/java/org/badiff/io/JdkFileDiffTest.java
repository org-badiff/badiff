package org.badiff.io;

import java.io.File;
import java.util.Arrays;

import org.badiff.DiffUtils;
import org.junit.Assert;
import org.junit.Test;

public class JdkFileDiffTest {

	@Test
	public void testWriteRead() throws Exception {
		byte[] orig = "Hello world!".getBytes();
		byte[] target = "Hellish cruel world!".getBytes();
		
		FileDiff fd = new JdkFileDiff(File.createTempFile("filediff", ".tmp"));
		fd.store(DiffUtils.improved(DiffUtils.queue(orig, target)));
		
		byte[] result = DiffUtils.apply(fd.queue(), orig);
		
		fd.delete();
		
		Assert.assertTrue(Arrays.equals(target, result));
		
	}

}
