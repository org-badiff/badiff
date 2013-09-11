package org.badiff.q;

import java.util.Collections;
import java.util.Comparator;

import org.badiff.Op;

public class UnchunkingOpQueue extends FilterOpQueue {
	
	protected static final Comparator<Op> OP_ORDER = new Comparator<Op>() {
		@Override
		public int compare(Op o1, Op o2) {
			Byte b1 = o1.getOp();
			Byte b2 = o2.getOp();
			return b1.compareTo(b2);
		}
	};

	public UnchunkingOpQueue(OpQueue source) {
		super(source);
	}

	@Override
	protected boolean pull() {
		if(!require(1))
			return flush();
		
		if(filtering.get(0).getOp() == Op.NEXT)
			return flush();
		
		for(Op e = source.poll(); e != null; e = source.poll()) {
			filtering.add(e);
			if(e.getOp() == Op.NEXT) {
				Collections.sort(filtering, OP_ORDER);
				return flush();
			}
		}
		
		Collections.sort(filtering, OP_ORDER);
		return flush();
	}
	
}
