package org.badiff.patcher.client;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.badiff.patcher.LocalRepository;
import org.junit.Before;
import org.junit.Test;

public class RepositoryClientTest {
	private LocalRepository local;
	private RepositoryClient client;
	
	@Before
	public void before() throws Exception {
		File root = new File("target/repo");
		FileUtils.deleteQuietly(root);
		root.mkdirs();
		
		local = new LocalRepository(root);
		local.commit(new File("src/test/resources/working_copies/0"));
		local.commit(new File("src/test/resources/working_copies/1"));
		local.commit(new File("src/test/resources/working_copies/2"));
		
		client = new RepositoryClient(new FileRepositoryAccess(local.getRoot()));
	}
	
	@Test
	public void testUpdateDigests() throws Exception {
		client.updateDigests();
		System.out.println(client.getDigests());
	}
	
	@Test
	public void testUpdateChain() throws Exception {
		client.updateChain();
		System.out.println(client.getChain().keys());
	}

}
