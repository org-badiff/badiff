package org.badiff.patcher.client;

import java.io.File;
import java.util.Map;

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
		local.commit(new File("src/test/resources/working_copies/3"));
		
		File storage = new File("target/client/storage");
		
		client = new RepositoryClient(new FileRepositoryAccess(root), storage);
	}
	
	@Test
	public void testUpdateDigests() throws Exception {
		client.updateDigests();
		System.out.println(client.getDigests());
	}
	
	@Test
	public void testUpdateChain() throws Exception {
		client.updateChain();
		System.out.println(client.getChain().getPaths());
	}
	
	@Test
	public void testPathAction() throws Exception {
		client.updateDigests();
		client.updateChain();
		PathAction pa = client.actionFor(new File("src/test/resources/working_copies/1"), "foo.txt", client.getDigests().get("foo.txt"));
		System.out.println(pa);
		
		pa = client.actionFor(new File("src/test/resources/working_copies/1"), "foo.txt", 0);
		System.out.println(pa);
		pa.load(client);
		System.out.println(pa);
		
		pa = client.actionFor(new File("src/test/resources/working_copies/3"), "bar.txt", Long.MAX_VALUE);
		System.out.println(pa);
	}
	
	@Test
	public void testPathActions() throws Exception {
		client.updateDigests();
		client.updateChain();
		
		Map<String, PathAction> actions = client.actionsFor(new File("src/test/resources/working_copies/3"), 0);
		System.out.println(actions);
	}

}
