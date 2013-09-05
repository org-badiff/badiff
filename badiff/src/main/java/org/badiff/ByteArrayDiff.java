package org.badiff;

import java.io.File;
import java.io.IOException;

import org.badiff.imp.MemoryDiff;
import org.badiff.imp.MemoryPatch;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.JdkSerialization;
import org.badiff.io.Serialization;
import org.badiff.util.Serials;

/**
 * Utilities for dealing with {@link Diff}s and {@link Patch}es as {@code byte[]}.
 * 
 * @author robin
 *
 */
public class ByteArrayDiff {

	/**
	 * The {@link Serialization} to use for persistence
	 */
	protected Serialization serial;
	
	/**
	 * Create a new {@link ByteArrayDiff} utilities instance
	 */
	public ByteArrayDiff() {
		this(DefaultSerialization.getInstance());
	}
	
	/**
	 * Create a new {@link ByteArrayDiff} utilities instance with a specified {@link Serialization}
	 * @param serial
	 */
	public ByteArrayDiff(Serialization serial) {
		this.serial = serial;
	}
	
	/**
	 * Compute and return a diff between {@code orig} and {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public byte[] diff(byte[] orig, byte[] target) {
		MemoryDiff md = new MemoryDiff();
		md.store(Diffs.improved(Diffs.queue(orig, target)));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}
	
	/**
	 * Apply {@code diff} to {@code orig} and return the result
	 * @param orig
	 * @param diff
	 * @return
	 */
	public byte[] apply(byte[] orig, byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		return Diffs.apply(md, orig);
	}

	/**
	 * Compute and return a patch from the contents of the directory {@code origRoot} to
	 * the contents of the directory {@code targetRoot}
	 * @param origRoot
	 * @param targetRoot
	 * @return
	 * @throws IOException
	 */
	public byte[] patch(File origRoot, File targetRoot) throws IOException {
		MemoryPatch mp = new MemoryPatch();
		mp.store(Patches.patch(origRoot, targetRoot));
		return Serials.serialize(serial, MemoryPatch.class, mp);
	}
	
	/**
	 * Apply, in-place, the {@code patch} to the directory {@code origRoot}
	 * @param origRoot
	 * @param patch
	 * @throws IOException
	 */
	public void apply(File origRoot, byte[] patch) throws IOException {
		MemoryPatch mp = Serials.deserialize(serial, MemoryPatch.class, patch);
		mp.apply(origRoot);
	}
}
