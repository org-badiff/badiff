package org.badiff.q;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.badiff.Op;

/**
 * {@link OpQueue} similar to {@link FilterOpQueue}, but draws its elements
 * from an evaluated {@link Future}{@code <OpQueue>} rather than a direct {@link OpQueue}.
 * @author robin
 *
 */
public class FutureOpQueue extends OpQueue {
	/**
	 * The {@link Future} that will supply an {@link OpQueue} to draw elements from
	 */
	protected Future<OpQueue> source;

	/**
	 * {@link OpQueue} that will draw elements from a {@link Future}{@code <OpQueue>}
	 * @param source
	 */
	public FutureOpQueue(Future<OpQueue> source) {
		this.source = source;
	}

	@Override
	public boolean offer(Op e) {
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
	
	/**
	 * Called when an element should be moved from {@link #source} to {@link OpQueue#pending}
	 */
	protected void filter() {
		if(pending.size() == 0)
			shiftPending();
	}
	
	/**
	 * Actually move an element from {@link #source} to {@link OpQueue#pending}
	 * @return
	 */
	protected boolean shiftPending() {
		try {
			Op e = source.get().poll();
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
