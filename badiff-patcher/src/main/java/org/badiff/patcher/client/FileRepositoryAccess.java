package org.badiff.patcher.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.badiff.patcher.util.Files;

public class FileRepositoryAccess implements RepositoryAccess {
	
	protected File root;
	
	public FileRepositoryAccess(File root) {
		this.root = root;
	}

	protected RemotePath toPath(File file) throws IOException {
		if(file.isFile())
			return new RemotePath(
					this,
					Files.relativePath(root, file),
					RemotePath.PathType.FILE,
					file.length(),
					file.lastModified());
		else if(file.isDirectory())
			return new RemotePath(
					this,
					Files.relativePath(root, file),
					RemotePath.PathType.DIRECTORY,
					0,
					0);
		else
			throw new IOException("not a file or directory:" + file);
	}
	
	@Override
	public RemotePath get(String path) throws IOException {
		return toPath(new File(root, path));
	}
	
	@Override
	public RemotePath[] list(RemotePath dir) throws IOException {
		File f = new File(root, dir.path());
		if(!f.isDirectory())
			throw new IOException("not a directory:" + f);
		File[] files = f.listFiles();
		RemotePath[] paths = new RemotePath[files.length];
		for(int i = 0; i < files.length; i++)
			paths[i] = toPath(files[i]);
		return paths;
	}

	@Override
	public InputStream open(RemotePath file) throws IOException {
		File f = new File(root, file.path());
		if(!f.isFile())
			throw new IOException("not a file:" + f);
		return new FileInputStream(f);
	}

}
