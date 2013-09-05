package org.badiff.q;

import java.util.List;

import org.badiff.Op;

/**
 * {@link OpQueue} that initially populates its {@link OpQueue#pending}
 * from a {@link List}
 * @author robin
 *
 */
public class ListOpQueue extends OpQueue {

	/**
	 * Return an {@link OpQueue} with pre-populated {@link OpQueue#pending}
	 * from the argument list
	 * @param ops
	 */
	public ListOpQueue(List<Op> ops) {
		for(Op e : ops)
			super.offer(e);
	}

}
