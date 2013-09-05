package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} that has no content
 * @author robin
 *
 */
public class EmptyInputStream extends InputStream {

	@Override
	public int read() throws IOException {
		return -1;
	}

}
