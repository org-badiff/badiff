package org.badiff.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferInput implements Input {
	
	protected ByteBuffer buf;
	
	public ByteBufferInput(ByteBuffer buf) {
		this.buf = buf;
	}
	
	public ByteBufferInput(byte[] buf) {
		this(ByteBuffer.wrap(buf));
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		buf.get(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		buf.get(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		n = Math.min(n, buf.remaining());
		n = Math.max(n, -buf.position());
		buf.position(buf.position() + n);
		return n;
	}

	@Override
	public boolean readBoolean() throws IOException {
		return buf.get() != 0;
	}

	@Override
	public byte readByte() throws IOException {
		return buf.get();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return 0xff & buf.get();
	}

	@Override
	public short readShort() throws IOException {
		return buf.getShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return 0xffff & buf.getShort();
	}

	@Override
	public char readChar() throws IOException {
		return buf.getChar();
	}

	@Override
	public int readInt() throws IOException {
		return buf.getInt();
	}

	@Override
	public long readLong() throws IOException {
		return buf.getLong();
	}

	@Override
	public float readFloat() throws IOException {
		return buf.getFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return buf.getDouble();
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long first() {
		return 0;
	}

	@Override
	public long last() {
		return buf.limit();
	}

	@Override
	public long position() {
		return buf.position();
	}

	@Override
	public void seek(long pos) throws IOException {
		buf.position((int) pos);
	}

	@Override
	public void skip(long count) throws IOException {
		skipBytes((int) count);
	}

	@Override
	public int read() throws IOException {
		try {
			return 0xff & buf.get();
		} catch(BufferUnderflowException e) {
			return -1;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		for(int i = 0; i < b.length; i++) {
			if(buf.remaining() == 0)
				return i;
			b[i] = buf.get();
		}
		return b.length;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		for(int i = 0; i < len; i++) {
			if(buf.remaining() == 0)
				return i;
			b[off + i] = buf.get();
		}
		return len;
	}

}
