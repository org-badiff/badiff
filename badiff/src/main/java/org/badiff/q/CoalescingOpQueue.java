package org.badiff.q;

import java.util.Arrays;

import org.badiff.DiffOp;

/**
 * {@link OpQueue} that entirely removes pairs of (INSERT,DELETE) {@link DiffOp}s
 * in the head of its {@link OpQueue#pending} queue if they have the same {@link DiffOp#getData()}
 * @author robin
 *
 */
public class CoalescingOpQueue extends FilterOpQueue {

	/**
	 * Create a {@link OpQueue} that coalesces pairs of equivalent (INSERT,DELETE).
	 * @param source
	 */
	public CoalescingOpQueue(OpQueue source) {
		super(source);
	}

	@Override
	protected void filter() {
		if(pending.size() == 0 && !shiftPending())
			return;
		if(pending.peekFirst().getOp() != DiffOp.INSERT)
			return;
		if(pending.size() == 1 && !shiftPending())
			return;
		
		DiffOp insert = pending.pollFirst();
		if(pending.peekFirst().getOp() != DiffOp.DELETE) {
			pending.offerFirst(insert);
			return;
		}
		DiffOp delete = pending.pollFirst();
		
		if(Arrays.equals(insert.getData(), delete.getData()))
			return;
		
		// bump the pair into the ready queue
		ready.offerLast(insert);
		ready.offerLast(delete);
	}

}
