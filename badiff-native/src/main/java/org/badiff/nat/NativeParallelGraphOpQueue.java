package org.badiff.nat;

import org.badiff.alg.Graph;
import org.badiff.q.OpQueue;
import org.badiff.q.ParallelGraphOpQueue;

public class NativeParallelGraphOpQueue extends ParallelGraphOpQueue {

	public NativeParallelGraphOpQueue(OpQueue source, int workers, int chunk) {
		super(source, workers, chunk);
	}

	public NativeParallelGraphOpQueue(OpQueue source) {
		super(source);
	}

	@Override
	protected Graph newGraph() {
		return new NativeGraph((chunk + 1) * (chunk + 1));
	}
	
}
