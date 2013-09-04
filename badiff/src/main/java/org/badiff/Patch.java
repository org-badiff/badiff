package org.badiff;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface Patch {
	public void apply(File root) throws IOException;
	
	public Set<String> keySet();
	public boolean containsKey(String path);
	public Diff get(String path);
	public Diff put(String path, Diff diff);
}
