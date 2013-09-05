package org.badiff.q;

import org.badiff.DiffOp;

public class FilterOpQueue extends OpQueue {
	protected OpQueue source;
	
	public FilterOpQueue(OpQueue source) {
		this.source = source;
	}
	
	@Override
	public boolean offer(DiffOp e) {
		return source.offer(e);
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
		DiffOp e = source.poll();
		if(e != null)
			pending.offerLast(e);
		return e != null;
	}

}
