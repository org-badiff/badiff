package org.badiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An object which can be applied to an "original" {@link InputStream} and write
 * the results to a "target" {@link OutputStream}.  Objects which implement
 * {@link Applyable} might not be re-usable; for some objects only one call to 
 * {@link #apply(InputStream, OutputStream)} will work.
 * @author robin
 *
 */
public interface Applyable {
	/**
	 * Apply this object to {@code orig} and write the result to {@code target}.
	 * @param orig
	 * @param target
	 * @throws IOException
	 */
	public void apply(InputStream orig, OutputStream target) throws IOException;

}
