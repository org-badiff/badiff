package org.badiff;

import java.io.IOException;
import java.util.Iterator;

/**
 * An object which can store a sequence of {@link Op}s
 * @author robin
 *
 */
public interface Storeable {
	/**
	 * Overwrite this object with the operations from the
	 * argument {@link Iterator}
	 * @param ops
	 * @throws IOException
	 */
	public void store(Iterator<Op> ops) throws IOException;

}
