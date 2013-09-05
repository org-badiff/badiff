package org.badiff.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with files
 * @author robin
 *
 */
public class Files {

	/**
	 * Create a temp directory
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
	public static File createTempDirectory(String prefix, String suffix) throws IOException {
		File dir = File.createTempFile(prefix, suffix);
		dir.delete();
		dir.mkdirs();
		return dir;
	}
	
	/**
	 * List the relative paths in a root directory, recursively
	 * @param prefix
	 * @param root
	 * @return
	 */
	public static List<String> listPaths(String prefix, File root) {
		List<String> paths = new ArrayList<String>();

		if(!root.isDirectory())
			return paths;
		
		for(File file : root.listFiles()) {
			if(file.isFile())
				paths.add(prefix + file.getName());
			if(!file.isDirectory())
				continue;
			paths.addAll(listPaths(prefix + "/" + file.getName(), file));
		}
		
		return paths;
	}
	
	private Files() {
	}

}
