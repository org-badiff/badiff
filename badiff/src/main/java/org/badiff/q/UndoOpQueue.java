package org.badiff.q;

import org.badiff.DiffOp;

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
		DiffOp e = pending.pollFirst();
		DiffOp u;
		if(e.getOp() == DiffOp.DELETE)
			u = new DiffOp(DiffOp.INSERT, e.getRun(), e.getData());
		else if(e.getOp() == DiffOp.INSERT)
			u = new DiffOp(DiffOp.DELETE, e.getRun(), e.getData());
		else
			u = e;
		ready.offerLast(u);
	}

}
