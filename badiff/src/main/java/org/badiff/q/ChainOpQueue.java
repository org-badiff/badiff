package org.badiff.q;

import java.util.ArrayDeque;
import java.util.Deque;

import org.badiff.DiffOp;

public class ChainOpQueue extends OpQueue {
	
	protected Deque<OpQueue> chain = new ArrayDeque<OpQueue>();

	public ChainOpQueue(OpQueue... links) {
		for(OpQueue q : links)
			offer(q);
	}
	
	public Deque<OpQueue> getChain() {
		return chain;
	}
	
	public boolean offer(OpQueue q) {
		return chain.offerLast(q);
	}
	
	@Override
	protected void shift() {
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
