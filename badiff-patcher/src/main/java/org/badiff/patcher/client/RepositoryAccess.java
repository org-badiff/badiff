package org.badiff.patcher.client;

import java.io.IOException;
import java.io.InputStream;

public interface RepositoryAccess {
	public RemotePath get(String path) throws IOException;
	public RemotePath[] list(RemotePath dir) throws IOException;
	public InputStream open(RemotePath file) throws IOException;
}
