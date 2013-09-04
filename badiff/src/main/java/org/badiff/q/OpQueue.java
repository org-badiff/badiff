package org.badiff.q;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import org.badiff.Op;

public class OpQueue {

	protected Deque<Op> ready = new ArrayDeque<Op>();
	protected Deque<Op> pending = new ArrayDeque<Op>();

	public Op poll() {
		Op e = ready.pollFirst();
		if(e == null) {
			shift();
			e = ready.pollFirst();
		}
		return e;
	}
	
	public boolean offer(Op e) {
		return pending.offerLast(e);
	}
	
	protected void shift() {
		shiftReady();
	}
	
	protected boolean shiftReady() {
		Op e = pending.pollFirst();
		if(e != null)
			ready.offerLast(e);
		return e != null;
	}
	
}
