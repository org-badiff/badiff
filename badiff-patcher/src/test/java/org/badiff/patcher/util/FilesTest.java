package org.badiff.patcher.util;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class FilesTest {
	@Test
	public void testRelativePath() throws Exception {
		File root = new File(".");
		
		String path;
		
		path = "src/test/java/org/badiff/patcher/util/FilesTest.java";
		Assert.assertEquals(path, Files.relativePath(root, new File(root, path)));
		
		try {
			String rel = Files.relativePath(root, new File(root, ".."));
			Assert.fail(rel + " should not be relative to " + root);
		} catch(IllegalArgumentException expected) {
		}
	}
}
