package org.badiff.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

public class StreamInput implements Input {
	
	protected PushbufferInputStream pin;
	protected DataInputStream din;

	public StreamInput(InputStream in, int size) {
		pin = new PushbufferInputStream(in, size);
		din = new DataInputStream(pin);
	}
	
	@Override
	public void readFully(byte[] b) throws IOException {
		din.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		din.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int) pin.skip(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return din.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return din.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return din.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return din.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return din.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		return din.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return din.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return din.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return din.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return din.readDouble();
	}

	@Deprecated
	@Override
	public String readLine() throws IOException {
		return din.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return din.readUTF();
	}

	@Override
	public long first() {
		return pin.first();
	}

	@Override
	public long last() {
		return Long.MAX_VALUE;
	}

	@Override
	public long position() {
		return pin.position();
	}

	@Override
	public void seek(long pos) throws IOException {
		pin.position(pos);
	}

	@Override
	public void skip(long count) throws IOException {
		pin.skip(count);
	}

	@Override
	public int read() throws IOException {
		return din.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return din.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return din.read(b, off, len);
	}

}
