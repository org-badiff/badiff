package org.badiff.q;

import org.badiff.DiffOp;

/**
 * {@link OpQueue} that is pre-populated with two {@link DiffOp}s, one
 * to {@link DiffOp#DELETE} a {@code byte[]} and one to {@link DiffOp#INSERT}
 * a {@code byte[]}
 * @author robin
 *
 */
public class ReplaceOpQueue extends OpQueue {

	/**
	 * Create an {@link OpQueue} populated with a {@link DiffOp#DELETE}
	 * and a {@link DiffOp#INSERT}
	 * @param orig
	 * @param target
	 */
	public ReplaceOpQueue(byte[] orig, byte[] target) {
		offer(new DiffOp(DiffOp.DELETE, orig.length, orig));
		offer(new DiffOp(DiffOp.INSERT, target.length, target));
	}

}
