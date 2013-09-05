package org.badiff.q;

import java.util.ArrayDeque;
import java.util.Deque;

import org.badiff.DiffOp;

/**
 * {@link OpQueue} that draws its {@link DiffOp} elements
 * from the head of a queue of other {@link OpQueue} instances,
 * discarding the head when it is emptied.  Lazy.
 * @author robin
 *
 */
public class ChainOpQueue extends OpQueue {
	
	/**
	 * The queue of {@link OpQueue} to draw elements from
	 */
	protected Deque<OpQueue> chain = new ArrayDeque<OpQueue>();

	/**
	 * Create a new {@link OpQueue} that draws its elements from the argument {@link OpQueue}s
	 * in a lazy manner, emptying each {@link OpQueue} before drawing any from the next.
	 * @param links
	 */
	public ChainOpQueue(OpQueue... links) {
		for(OpQueue q : links)
			offer(q);
	}
	
	/**
	 * Returns the queue of {@link OpQueue} used to draw elements from
	 * @return
	 */
	public Deque<OpQueue> getChain() {
		return chain;
	}
	
	/**
	 * Add another {@link OpQueue} to the chain
	 * @param q
	 * @return
	 */
	public boolean offer(OpQueue q) {
		return chain.offerLast(q);
	}
	
	@Override
	protected void shift() {
		/*
		 * If there are no pending DiffOp's, then try to draw one from
		 * the head of the chain until either there is a pending DiffOp
		 * or the chain is empty.
		 */
		while(pending.size() == 0 && chain.size() > 0) {
			DiffOp e = chain.peekFirst().poll();
			if(e == null) {
				chain.pollFirst();
				continue;
			}
			pending.offerLast(e);
		}
		super.shift();
	}

}
