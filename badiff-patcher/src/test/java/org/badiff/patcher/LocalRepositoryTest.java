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
		repo.commit(new File("src/test/resources/working_copies/0"));
		repo.commit(new File("src/test/resources/working_copies/1"));
		repo.commit(new File("src/test/resources/working_copies/2"));
		repo.commit(new File("src/test/resources/working_copies/3"));
	}
}
