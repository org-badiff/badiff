package org.badiff;

import java.io.IOException;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.Serialization;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.UndoOpQueue;
import org.badiff.util.Diffs;

public class MemoryDiffs {

	protected Serialization serial;
	
	public MemoryDiffs() {
		this(DefaultSerialization.getInstance());
	}
	
	public MemoryDiffs(Serialization serial) {
		this.serial = serial;
	}

	/**
	 * Compute and return a diff between {@code orig} and {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public MemoryDiff diff(byte[] orig, byte[] target) {
		return new MemoryDiff(Diffs.improved(Diffs.queue(orig, target)));
	}
	
	/**
	 * Apply {@code diff} to {@code orig} and return the result
	 * @param orig
	 * @param diff
	 * @return
	 */
	public byte[] apply(byte[] orig, Diff diff) {
		return Diffs.apply(diff, orig);
	}
	
	/**
	 * Compute and return a one-way (unidirectional) diff from {@code orig} to {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public MemoryDiff udiff(byte[] orig, byte[] target) {
		return new MemoryDiff(new OneWayOpQueue(Diffs.improved(Diffs.queue(orig, target))));
	}
	
	/**
	 * Apply the inverse of {@code diff} to {@code target} and return the result
	 * @param target
	 * @param diff
	 * @return
	 */
	public byte[] undo(byte[] target, Diff diff) throws IOException {
		return Diffs.apply(new UndoOpQueue(diff.queue()), target);
	}
	
	/**
	 * Compute and return a one-way (unidirectional) diff given any diff
	 * @param diff
	 * @return
	 */
	public MemoryDiff udiff(Diff diff) throws IOException {
		return new MemoryDiff(new OneWayOpQueue(diff.queue()));
	}
	
	/**
	 * Compute and return an "undo" diff from a two-way diff
	 * @param diff
	 * @return
	 */
	public MemoryDiff undo(Diff diff) throws IOException {
		return new MemoryDiff(new UndoOpQueue(diff.queue()));
	}

}
