package org.badiff.q;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.badiff.BADiff;
import org.badiff.Op;

public class OpQueue implements BADiff {

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
	
	public void drainTo(List<Op> c) {
		for(Op e = poll(); e != null; e = poll())
			c.add(e);
	}
	
	public void drainTo(OpQueue q) {
		for(Op e = poll(); e != null; e = poll())
			q.offer(e);
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

	@Override
	public void applyDiff(InputStream orig, OutputStream target)
			throws IOException {
		for(Op e = poll(); e != null; e = poll())
			e.applyOp(orig, target);
	}
	
}
