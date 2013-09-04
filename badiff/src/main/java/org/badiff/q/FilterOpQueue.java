package org.badiff.q;

import org.badiff.Op;

public class FilterOpQueue extends OpQueue {
	protected OpQueue source;
	
	public FilterOpQueue(OpQueue source) {
		this.source = source;
	}
	
	@Override
	public boolean offer(Op e) {
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
		Op e = source.poll();
		if(e != null)
			pending.offerLast(e);
		return e != null;
	}

}
