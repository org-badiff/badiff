package org.badiff.q;

import org.badiff.Op;

/**
 * {@link OpQueue} that is pre-populated with two {@link Op}s, one
 * to {@link Op#DELETE} a {@code byte[]} and one to {@link Op#INSERT}
 * a {@code byte[]}
 * @author robin
 *
 */
public class ReplaceOpQueue extends OpQueue {

	/**
	 * Create an {@link OpQueue} populated with a {@link Op#DELETE}
	 * and a {@link Op#INSERT}
	 * @param orig
	 * @param target
	 */
	public ReplaceOpQueue(byte[] orig, byte[] target) {
		offer(new Op(Op.DELETE, orig.length, orig));
		offer(new Op(Op.INSERT, target.length, target));
	}

}
