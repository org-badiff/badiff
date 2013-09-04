package org.badiff.q;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.badiff.Diff;
import org.badiff.Op;

public class OpQueue implements Iterator<Op> {

	protected Op iterNext;
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
	
	public <T extends List<Op>> T drainTo(T c) {
		for(Op e = poll(); e != null; e = poll())
			c.add(e);
		return c;
	}
	
	public <T extends OpQueue> T drainTo(T q) {
		for(Op e = poll(); e != null; e = poll())
			q.offer(e);
		return q;
	}
	
	public <T extends Diff> T drainTo(T diff) throws IOException {
		diff.storeDiff(this);
		return diff;
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

	public void applyQueue(InputStream orig, OutputStream target)
			throws IOException {
		for(Op e = poll(); e != null; e = poll())
			e.applyOp(orig, target);
	}

	@Override
	public boolean hasNext() {
		if(iterNext == null)
			iterNext = poll();
		return iterNext != null;
	}

	@Override
	public Op next() {
		if(iterNext == null)
			iterNext = poll();
		return iterNext;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
