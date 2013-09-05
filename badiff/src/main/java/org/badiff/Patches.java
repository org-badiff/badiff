package org.badiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.badiff.io.EmptyInputStream;
import org.badiff.io.MemoryDiff;

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
	
	private Patches() {
	}

}
