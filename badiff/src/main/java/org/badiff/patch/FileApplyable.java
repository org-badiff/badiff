package org.badiff.patch;

import java.io.File;
import java.io.IOException;

/**
 * An object which can be applied to a {@link File}, either in-place,
 * or with a separate target. 
 * @author robin
 *
 */
public interface FileApplyable {
	/**
	 * Apply this operation to the {@code orig} file.
	 * @param orig
	 * @throws IOException
	 */
	public void apply(File orig) throws IOException;
	/**
	 * Apply this operation to the {@code target} file using the {@code orig} file
	 * as input
	 * @param orig
	 * @param target
	 * @throws IOException
	 */
	public void apply(File orig, File target) throws IOException;
}
