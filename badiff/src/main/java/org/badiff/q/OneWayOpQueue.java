package org.badiff.q;

import org.badiff.Op;

/**
 * {@link OpQueue} that strips the {@link Op#getData()} from {@link Op#DELETE}s
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
		Op e = pending.pollFirst();
		if(e.getOp() == Op.DELETE)
			e = new Op(Op.DELETE, e.getRun(), null);
		ready.offerLast(e);
	}

}
