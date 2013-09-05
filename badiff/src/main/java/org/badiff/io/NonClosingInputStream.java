package org.badiff.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link FilterInputStream} which doesn't close its wrapped {@link InputStream}
 * @author robin
 *
 */
public class NonClosingInputStream extends FilterInputStream {

	public NonClosingInputStream(InputStream in) {
		super(in);
	}

	@Override
	public void close() throws IOException {
	}
}
