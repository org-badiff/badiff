package org.badiff.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class RandomInputStream extends InputStream {
	protected RandomInput in;
	protected long mark = -1;
	
	public RandomInputStream(File file) throws IOException {
		this(new FileRandomInput(file));
	}
	
	public RandomInputStream(byte[] buf) {
		this(new ByteBufferRandomInput(buf));
	}
	
	public RandomInputStream(ByteBuffer buf) {
		this(new ByteBufferRandomInput(buf));
	}
	
	public RandomInputStream(RandomInput in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public synchronized void mark(int readlimit) {
		mark = in.position();
	}

	@Override
	public synchronized void reset() throws IOException {
		if(mark == -1)
			throw new IOException("unset mark");
		in.seek(mark);
	}

	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}

}
