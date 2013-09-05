package org.badiff.q;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.badiff.DiffOp;

public class FutureOpQueue extends OpQueue {
	protected Future<OpQueue> source;

	public FutureOpQueue(Future<OpQueue> source) {
		this.source = source;
	}

	@Override
	public boolean offer(DiffOp e) {
		try {
			return source.get().offer(e);
		} catch(InterruptedException ie) {
			throw new RuntimeException(ie);
		} catch(ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}
	
	@Override
	protected void shift() {
		filter();
		super.shift();
	}
	
	protected void filter() {
		if(pending.size() == 0)
			shiftPending();
	}
	
	protected boolean shiftPending() {
		try {
			DiffOp e = source.get().poll();
			if(e != null)
				pending.offerLast(e);
			return e != null;
		} catch(InterruptedException ie) {
			throw new RuntimeException(ie);
		} catch(ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}

}
