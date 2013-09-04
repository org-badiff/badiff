package org.badiff.q;

import java.util.ArrayDeque;
import java.util.Deque;

import org.badiff.Op;

public class ChainOpQueue extends OpQueue {
	
	protected Deque<OpQueue> chain = new ArrayDeque<OpQueue>();

	public ChainOpQueue(OpQueue... links) {
		for(OpQueue q : links)
			offer(q);
	}
	
	public boolean offer(OpQueue q) {
		return chain.offerLast(q);
	}
	
	@Override
	protected void shift() {
		while(pending.size() == 0 && chain.size() > 0) {
			Op e = chain.peekFirst().poll();
			if(e == null) {
				chain.pollFirst();
				continue;
			}
			pending.offerLast(e);
		}
		super.shift();
	}

}
