package org.badiff.patcher;

import java.io.File;

public class LocalRepository {
	protected File root;
	
	public LocalRepository(File root) {
		if(!root.isDirectory())
			throw new IllegalArgumentException("repository root must be a directory");
		this.root = root;
	}
	
	public File getRoot() {
		return root;
	}
	
	public File getWorkingCopyRoot() {
		return new File(root, "working_copy");
	}
	
	public File getPathDiffsRoot() {
		return new File(root, "diffs");
	}
}
