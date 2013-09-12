package org.badiff.q;

import java.util.Arrays;

import org.badiff.Op;

public class CompactingOpQueue extends FilterOpQueue {

	public CompactingOpQueue(OpQueue source) {
		super(source);
	}

	@Override
	protected boolean pull() {
		if(!require(2))
			return flush();

		while(filtering.get(0).getOp() == filtering.get(1).getOp()) {
			Op e1 = filtering.remove(0);
			Op e2 = filtering.remove(0);
			byte[] data = null;
			if(e1.getData() != null && e2.getData() != null) {
				data = Arrays.copyOf(e1.getData(), e1.getData().length + e2.getData().length);
				System.arraycopy(e2.getData(), 0, data, e1.getData().length, e2.getData().length);
			}
			filtering.add(0, new Op(e1.getOp(), e1.getRun() + e2.getRun(), data));
			if(!require(2))
				return flush();
		}
		
		prepare(filtering.remove(0));
		return true;
	}
	
}
