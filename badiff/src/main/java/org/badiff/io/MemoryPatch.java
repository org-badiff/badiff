package org.badiff.io;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.badiff.Diff;
import org.badiff.DiffUtils;
import org.badiff.Patch;
import org.badiff.PatchOp;

public class MemoryPatch extends TreeMap<String, PatchOp> implements Patch {

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

}
