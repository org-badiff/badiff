package org.badiff.q;

import org.badiff.Op;

public class ReplaceOpQueue extends OpQueue {

	public ReplaceOpQueue(byte[] orig, byte[] target) {
		offer(new Op(Op.DELETE, orig.length, orig));
		offer(new Op(Op.INSERT, target.length, target));
	}

}
