package org.badiff.q;

import java.util.Arrays;

import org.badiff.Op;

public class CoalescingOpQueue extends FilterOpQueue {

	public CoalescingOpQueue(OpQueue source) {
		super(source);
	}

	@Override
	protected void filter() {
		if(pending.size() >= 2)
			return;
		if(pending.size() == 0 && !shiftPending())
			return;
		if(pending.peekFirst().getOp() != Op.INSERT)
			return;
		if(pending.size() == 1 && !shiftPending())
			return;
		
		Op insert = pending.pollFirst();
		if(pending.peekFirst().getOp() != Op.DELETE) {
			pending.offerFirst(insert);
			return;
		}
		Op delete = pending.pollFirst();
		
		if(Arrays.equals(insert.getData(), delete.getData()))
			return;
		
		pending.offerFirst(delete);
		pending.offerFirst(insert);
	}

}
