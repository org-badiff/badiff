package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

public class PushbufferInputStream extends InputStream {

	protected PushbackInputStream in;
	protected long pos;
	protected ByteBuffer buf;
	
	public PushbufferInputStream(InputStream in, int size) {
		this.in = new PushbackInputStream(in, size);
		this.buf = ByteBuffer.allocate(size);
	}
	
	public long position() {
		return pos;
	}
	
	public void position(long pos) throws IOException {
		skip(pos - this.pos);
	}
	
	public long first() {
		return pos - buf.position();
	}
	
	@Override
	public long skip(long n) throws IOException {
		long skipped = 0;
		while(n > skipped) {
			read();
			skipped++;
		}
		if(n < 0) {
			buf.position(buf.position() + (int) n);
			byte[] b = new byte[(int) -n];
			buf.get(b);
			buf.position(buf.position() + (int) n);
			in.unread(b);
			skipped = n;
		}
		pos += skipped;
		return skipped;
	}

	@Override
	public int read() throws IOException {
		int r = in.read();
		if(r >= 0) {
			if(buf.remaining() == 0) {
				buf.get();
				buf.compact();
			}
			buf.put((byte) r);
			pos++;
		}
		return r;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int r = in.read(b);
		if(r >= 0) {
			if(buf.remaining() < r) {
				buf.position(buf.limit() - r);
				buf.compact();
			}
			buf.put(b, 0, r);
			pos += r;
		}
		return r;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r = in.read(b, off, len);
		if(r >= 0) {
			if(buf.remaining() < r) {
				buf.position(buf.limit() - r);
				buf.compact();
			}
			buf.put(b, off, r);
			pos += r;
		}
		return r;
	}
	
}
