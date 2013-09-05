package org.badiff.q;

import org.badiff.DiffOp;

public class ReplaceOpQueue extends OpQueue {

	public ReplaceOpQueue(byte[] orig, byte[] target) {
		offer(new DiffOp(DiffOp.DELETE, orig.length, orig));
		offer(new DiffOp(DiffOp.INSERT, target.length, target));
	}

}
