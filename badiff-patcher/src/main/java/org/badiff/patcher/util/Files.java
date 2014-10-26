package org.badiff.patcher.util;

import java.io.File;
import java.io.IOException;

public abstract class Files {

	public static boolean isChild(File root, File file) throws IOException {
		if(root == null)
			throw new IllegalArgumentException();
		if(file == null)
			return false;
		root = root.getCanonicalFile();
		file = file.getCanonicalFile();
		if(root.equals(file))
			return true;
		return isChild(root, file.getParentFile());
	}
	
	public static String relativePath(File root, File file) throws IOException {
		if(root == null || file == null)
			throw new IllegalArgumentException();
		if(!isChild(root, file))
			throw new IllegalArgumentException(file + " is not a child of " + root);
		root = root.getCanonicalFile();
		file = file.getCanonicalFile();
		if(root.equals(file))
			return "";
		String rel = relativePath(root, file.getParentFile()) + "/" + file.getName();
		if(rel.startsWith("/"))
			rel = rel.substring(1);
		return rel;
	}
	
	private Files() {}
}
