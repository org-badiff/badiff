package org.badiff.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link FilterOutputStream} which doesn't close its wrapped {@link OutputStream}
 * @author robin
 *
 */
public class NonClosingOutputStream extends FilterOutputStream {

	public NonClosingOutputStream(OutputStream out) {
		super(out);
	}

	@Override
	public void close() throws IOException {
	}
}
