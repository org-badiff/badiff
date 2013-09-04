package org.badiff.io;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.badiff.Patch;
import org.badiff.PatchOp;

public class MemoryPatch extends TreeMap<String, PatchOp> implements Patch {
	private static final long serialVersionUID = 0;

	@Override
	public void apply(File orig, File target) throws IOException {
		for(String path : keySet()) {
			PatchOp op = get(path);
			op.apply(new File(orig, path), new File(target, path));
		}
	}
	
	@Override
	public void apply(File orig) throws IOException {
		for(String path : keySet()) {
			PatchOp op = get(path);
			op.apply(new File(orig, path));
		}
	}
	
	@Override
	public boolean containsKey(String path) {
		return containsKey((Object) path);
	}

	@Override
	public PatchOp get(String path) {
		return get((Object) path);
	}

	@Override
	public void store(Patch other) {
		clear();
		for(String path : other.keySet())
			put(path, other.get(path));
	}

}
