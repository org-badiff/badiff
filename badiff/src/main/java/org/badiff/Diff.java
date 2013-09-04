package org.badiff;

import org.badiff.q.OpQueue;
import org.badiff.q.ReplaceOpQueue;

public class Diff {
	
	public static OpQueue queue(byte[] orig, byte[] target) {
		return new ReplaceOpQueue(orig, target);
	}

	private Diff() {
	}

}
