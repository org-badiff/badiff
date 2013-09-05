package org.badiff.io;

import java.io.IOException;
import java.io.OutputStream;

public class NoopOutputStream extends OutputStream {

	public NoopOutputStream() {
	}

	@Override
	public void write(int b) throws IOException {
	}

	@Override
	public void write(byte[] b) throws IOException {
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
	}
	
}
