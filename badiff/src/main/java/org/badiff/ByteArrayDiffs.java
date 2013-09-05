package org.badiff;

import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.Serialization;
import org.badiff.patch.Patch;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.UndoOpQueue;
import org.badiff.util.Serials;

/**
 * Utilities for dealing with {@link Diff}s and {@link Patch}es as {@code byte[]}.
 * 
 * @author robin
 *
 */
public class ByteArrayDiffs {

	/**
	 * The {@link Serialization} to use for persistence
	 */
	protected Serialization serial;
	
	/**
	 * Create a new {@link ByteArrayDiffs} utilities instance
	 */
	public ByteArrayDiffs() {
		this(DefaultSerialization.getInstance());
	}
	
	/**
	 * Create a new {@link ByteArrayDiffs} utilities instance with a specified {@link Serialization}
	 * @param serial
	 */
	public ByteArrayDiffs(Serialization serial) {
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
	 * Compute and return a one-way (unidirectional) diff from {@code orig} to {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public byte[] udiff(byte[] orig, byte[] target) {
		MemoryDiff md = new MemoryDiff();
		md.store(new OneWayOpQueue(Diffs.improved(Diffs.queue(orig, target))));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}
	
	/**
	 * Apply the inverse of {@code diff} to {@code target} and return the result
	 * @param target
	 * @param diff
	 * @return
	 */
	public byte[] undo(byte[] target, byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		return Diffs.apply(new UndoOpQueue(md.queue()), target);
	}

	/**
	 * Compute and return a one-way (unidirectional) diff given any diff
	 * @param diff
	 * @return
	 */
	public byte[] udiff(byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		md.store(new OneWayOpQueue(md.queue()));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}
	
	/**
	 * Compute and return an "undo" diff from a two-way diff
	 * @param diff
	 * @return
	 */
	public byte[] undo(byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		md.store(new UndoOpQueue(md.queue()));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}

}
