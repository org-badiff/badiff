package org.badiff.q;

import java.util.List;

import org.badiff.Op;

public class ListOpQueue extends OpQueue {

	public ListOpQueue(List<Op> ops) {
		for(Op e : ops)
			offer(e);
	}

}
