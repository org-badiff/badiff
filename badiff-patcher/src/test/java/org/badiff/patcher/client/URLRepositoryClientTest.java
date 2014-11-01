package org.badiff.patcher.client;

public class URLRepositoryClientTest extends RepositoryClientTest {
	@Override
	public void before() throws Exception {
		super.before();
		client = new RepositoryClient(new URLRepositoryAccess(root.toURI().toURL()), storage);
	}
}	
