package org.badiff.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Files {

	public static File createTempDirectory(String prefix, String suffix) throws IOException {
		File dir = File.createTempFile(prefix, suffix);
		dir.delete();
		dir.mkdirs();
		return dir;
	}
	
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
