package org.badiff.q;

import java.util.List;

import org.badiff.DiffOp;

public class ListOpQueue extends OpQueue {

	public ListOpQueue(List<DiffOp> ops) {
		for(DiffOp e : ops)
			super.offer(e);
	}

}
