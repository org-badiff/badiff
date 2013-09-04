package org.badiff.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NonClosingOutputStream extends FilterOutputStream {

	public NonClosingOutputStream(OutputStream out) {
		super(out);
	}

	@Override
	public void close() throws IOException {
	}
}
