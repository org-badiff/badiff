package org.badiff.util;

import java.io.File;
import java.io.IOException;

public class Files {

	public static File createTempDirectory(String prefix, String suffix) throws IOException {
		File dir = File.createTempFile(prefix, suffix);
		dir.delete();
		dir.mkdirs();
		return dir;
	}
	
	private Files() {
	}

}
