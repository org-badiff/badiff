package org.badiff.patcher.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

public class RemotePath {
	public static final Comparator<RemotePath> LAST_MODIFIED_ORDER = new Comparator<RemotePath>() {
		@Override
		public int compare(RemotePath o1, RemotePath o2) {
			return ((Long) o1.lastModified()).compareTo(o2.lastModified());
		}
	};
	
	public static enum PathType {
		FILE,
		DIRECTORY,
	}
	
	protected RepositoryAccess access;
	protected String path;
	protected PathType type;
	protected long length;
	protected long lastModified;
	
	public RemotePath(RepositoryAccess access, String path, PathType type, long length, long lastModified) {
		this.access = access;
		this.path = path;
		this.type = type;
		this.length = length;
		this.lastModified = lastModified;
	}
	
	public RepositoryAccess access() {
		return access;
	}
	
	public String path() {
		return path;
	}
	
	public PathType type() {
		return type;
	}
	
	public long length() {
		return length;
	}
	
	public long lastModified() {
		return lastModified;
	}
	
	public String name() {
		return path.replaceAll(".*/", "");
	}
	
	public InputStream open() throws IOException {
		return access().open(this);
	}
	
	public RemotePath[] list() throws IOException {
		return access().list(this);
	}
}
