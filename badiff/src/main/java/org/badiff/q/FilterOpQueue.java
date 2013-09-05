package org.badiff.q;

import org.badiff.DiffOp;

/**
 * {@link OpQueue} that draws its pending elements from another wrapped {@link OpQueue}
 * @author robin
 *
 */
public class FilterOpQueue extends OpQueue {
	/**
	 * The source of elements for this {@link FilterOpQueue}
	 */
	protected OpQueue source;
	
	/**
	 * Create a new {@link OpQueue} that draws elements into {@link OpQueue#pending}
	 * by calling {@link OpQueue#poll()} on the argument
	 * @param source
	 */
	public FilterOpQueue(OpQueue source) {
		this.source = source;
	}
	
	/**
	 * Offers the element to the wrapped {@link OpQueue}
	 */
	@Override
	public boolean offer(DiffOp e) {
		return source.offer(e);
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
		DiffOp e = source.poll();
		if(e != null)
			pending.offerLast(e);
		return e != null;
	}

}
