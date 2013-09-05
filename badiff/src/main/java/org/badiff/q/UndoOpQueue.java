package org.badiff.q;

import org.badiff.Op;

public class UndoOpQueue extends FilterOpQueue {

	public UndoOpQueue(OpQueue source) {
		super(source);
	}
	
	@Override
	protected void filter() {
		if(ready.size() > 0)
			return;
		if(pending.size() == 0 && !shiftPending())
			return;
		Op e = pending.pollFirst();
		Op u;
		if(e.getOp() == Op.DELETE)
			u = new Op(Op.INSERT, e.getRun(), e.getData());
		else if(e.getOp() == Op.INSERT)
			u = new Op(Op.DELETE, e.getRun(), e.getData());
		else
			u = e;
		ready.offerLast(u);
	}

}
