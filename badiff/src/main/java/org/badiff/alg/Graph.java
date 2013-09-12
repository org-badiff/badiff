package org.badiff.alg;

import org.badiff.q.OpQueue;

public interface Graph {
	public void compute(byte[] orig, byte[] target);
	public OpQueue queue();
}
