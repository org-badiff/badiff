package org.badiff.q;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.badiff.Applyable;
import org.badiff.Diff;
import org.badiff.DiffOp;

public class OpQueue implements Applyable, Iterator<DiffOp> {

	protected DiffOp iterNext;
	protected Deque<DiffOp> ready = new ArrayDeque<DiffOp>();
	protected Deque<DiffOp> pending = new ArrayDeque<DiffOp>();

	public DiffOp poll() {
		DiffOp e = ready.pollFirst();
		if(e == null) {
			shift();
			e = ready.pollFirst();
		}
		return e;
	}
	
	public boolean offer(DiffOp e) {
		return pending.offerLast(e);
	}
	
	public <T extends List<DiffOp>> T drainTo(T c) {
		for(DiffOp e = poll(); e != null; e = poll())
			c.add(e);
		return c;
	}
	
	public <T extends OpQueue> T drainTo(T q) {
		for(DiffOp e = poll(); e != null; e = poll())
			q.offer(e);
		return q;
	}
	
	public <T extends Diff> T drainTo(T diff) throws IOException {
		diff.store(this);
		return diff;
	}
	
	protected void shift() {
		shiftReady();
	}
	
	protected boolean shiftReady() {
		DiffOp e = pending.pollFirst();
		if(e != null)
			ready.offerLast(e);
		return e != null;
	}

	public void apply(InputStream orig, OutputStream target)
			throws IOException {
		for(DiffOp e = poll(); e != null; e = poll())
			e.apply(orig, target);
	}

	@Override
	public boolean hasNext() {
		if(iterNext == null)
			iterNext = poll();
		return iterNext != null;
	}

	@Override
	public DiffOp next() {
		if(iterNext == null)
			iterNext = poll();
		try {
			return iterNext;
		} finally {
			iterNext = null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
