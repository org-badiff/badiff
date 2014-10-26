package org.badiff.patcher;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class LocalRepositoryTest {
	private LocalRepository repo;
	
	@Before
	public void before() throws Exception {
		File root = new File("target/repo");
		FileUtils.deleteQuietly(root);
		root.mkdirs();
		repo = new LocalRepository(root);
	}
	
	@Test
	public void testCommit() throws Exception {
		repo.commit(new File("src"));
	}
}
