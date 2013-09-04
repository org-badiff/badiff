package org.badiff.io;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.badiff.Diff;
import org.badiff.Patch;

public class MemoryPatch extends TreeMap<String, Diff> implements Patch {

	@Override
	public void apply(File root) throws IOException {
		for(String path : keySet()) {
			File file = new File(root, path);
			Diff diff = get(path);
		}
	}

	@Override
	public boolean containsKey(String path) {
		return containsKey((Object) path);
	}

	@Override
	public Diff get(String path) {
		return get((Object) path);
	}

}
