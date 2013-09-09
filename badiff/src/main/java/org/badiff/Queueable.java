package org.badiff;

import java.io.IOException;

import org.badiff.q.OpQueue;

public interface Queueable {
	/**
	 * Return this object's operations.  This queue
	 * be {@link OpQueue#poll()}'d from but not {@link OpQueue#offer(Op)}'d to.
	 * @return
	 * @throws IOException
	 */
	public OpQueue queue() throws IOException;

}
