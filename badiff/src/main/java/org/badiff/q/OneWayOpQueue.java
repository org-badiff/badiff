package org.badiff.q;

import org.badiff.DiffOp;

/**
 * {@link OpQueue} that strips the {@link DiffOp#getData()} from {@link DiffOp#DELETE}s
 * @author robin
 *
 */
public class OneWayOpQueue extends FilterOpQueue {

	public OneWayOpQueue(OpQueue source) {
		super(source);
	}

	@Override
	protected void filter() {
		if(ready.size() > 0)
			return;
		if(pending.size() == 0 && !shiftPending())
			return;
		DiffOp e = pending.pollFirst();
		if(e.getOp() == DiffOp.DELETE)
			e = new DiffOp(DiffOp.DELETE, e.getRun(), null);
		ready.offerLast(e);
	}

}
