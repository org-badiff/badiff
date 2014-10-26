package org.badiff.patcher.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RepositoryClient {
	protected RepositoryAccess access;
	
	protected PathDiffChain chain;
	
	public RepositoryClient(RepositoryAccess access) {
		this.access = access;
		chain = new PathDiffChain();
	}
	
	public void updateChain() throws IOException {
		List<RemotePath> diffs = Arrays.asList(access.get("diffs").list());
		Collections.sort(diffs, RemotePath.LAST_MODIFIED_ORDER);
		for(RemotePath d : diffs) {
			chain.add(new PathDiffLink(d.name()));
		}
	}
}
