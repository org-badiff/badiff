package org.badiff.patch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.badiff.Diff;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.EmptyInputStream;
import org.badiff.util.Diffs;
import org.badiff.util.Files;

/**
 * Utility methods for dealing with patches
 * @author robin
 *
 */
public class Patches {

	public static PatchOp patchOp(File orig, File target) throws IOException {
		if(!orig.exists() && !target.exists())
			return new PatchOp(PatchOp.SKIP, null);
		if(!target.exists())
			return new PatchOp(PatchOp.DELETE, null);
		if(!orig.exists()) {
			Diff diff = new MemoryDiff();
			InputStream tin = new FileInputStream(target);
			diff.store(Diffs.queue(new EmptyInputStream(), tin));
			tin.close();
			return new PatchOp(PatchOp.CREATE, diff);
		}
		
		Diff diff = new MemoryDiff();
		InputStream oin = new FileInputStream(orig);
		InputStream tin = new FileInputStream(target);
		diff.store(Diffs.improved(Diffs.queue(oin, tin)));
		tin.close();
		oin.close();
		return new PatchOp(PatchOp.DIFF, diff);
	}
	
	public static Patch patch(File origRoot, File targetRoot) throws IOException {
		List<String> origPaths = Files.listPaths("", origRoot);
		List<String> destPaths = Files.listPaths("", targetRoot);
		
		Set<String> paths = new TreeSet<String>();
		paths.addAll(origPaths);
		paths.addAll(destPaths);
		
		Patch patch = new MemoryPatch();
		for(String path : paths) {
			File orig = new File(origRoot, path);
			File target = new File(targetRoot, path);
			patch.put(path, patchOp(orig, target));
		}
		
		return patch;
	}
	
	private Patches() {
	}

}
