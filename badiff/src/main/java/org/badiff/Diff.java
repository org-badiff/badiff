package org.badiff;

import java.io.IOException;
import java.util.Iterator;

import org.badiff.q.OpQueue;

/**
 * A byte-level difference between two inputs.  Can be applied to streams
 * via {@link Applyable}.  {@link Diff} is a <b>re-usable</b> instance of {@link Applyable}.
 * @author robin
 *
 */
public interface Diff extends Applyable {
	/**
	 * The default size of a chunk for operations which chunk their input
	 */
	public final int DEFAULT_CHUNK = 1024;
	
	/**
	 * Overwrite this {@link Diff}'s operations with the operations from the
	 * argument {@link Iterator}
	 * @param ops
	 * @throws IOException
	 */
	public void store(Iterator<DiffOp> ops) throws IOException;
	
	/**
	 * Return a copy of this {@link Diff}'s operations.  This copy may
	 * be {@link OpQueue#poll()}'d from but not {@link OpQueue#offer(DiffOp)}'d to.
	 * @return
	 * @throws IOException
	 */
	public OpQueue queue() throws IOException;
}
