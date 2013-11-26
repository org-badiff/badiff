package org.badiff.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;

public class FileRandomInput extends FilterInputStream implements RandomInput {
	protected File file;
	protected long pos;
	protected DataInputStream data;
	
	public FileRandomInput(File file) throws IOException {
		super(new FileInputStream(file));
		this.file = file;
		data = new DataInputStream(this);
	}

	@Override
	public long first() {
		return 0;
	}

	@Override
	public long last() {
		return file.length();
	}

	@Override
	public long position() {
		return pos;
	}

	@Override
	public void seek(long pos) throws IOException {
		skip(pos - this.pos);
	}

	@Override
	public int read() throws IOException {
		int b = super.read();
		if(b >= 0)
			pos++;
		return b;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int r = super.read(b);
		if(r > 0)
			pos += r;
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r = super.read(b, off, len);
		if(r > 0)
			pos += r;
		return r;
	}

	@Override
	public long skip(long n) throws IOException {
		if(n > 0) {
			long s = in.skip(n);
			pos += s;
			return s;
		} else if(n < 0) {
			in.close();
			in = new FileInputStream(file);
			in.skip(pos + n);
			pos = pos + n;
			return n;
		} else
			return 0;
	}

	public final void readFully(byte[] b) throws IOException {
		data.readFully(b);
	}

	public final void readFully(byte[] b, int off, int len) throws IOException {
		data.readFully(b, off, len);
	}

	public final boolean readBoolean() throws IOException {
		return data.readBoolean();
	}

	public final byte readByte() throws IOException {
		return data.readByte();
	}

	public final int readUnsignedByte() throws IOException {
		return data.readUnsignedByte();
	}

	public final short readShort() throws IOException {
		return data.readShort();
	}

	public final int readUnsignedShort() throws IOException {
		return data.readUnsignedShort();
	}

	public final char readChar() throws IOException {
		return data.readChar();
	}

	public final int readInt() throws IOException {
		return data.readInt();
	}

	public final long readLong() throws IOException {
		return data.readLong();
	}

	public final float readFloat() throws IOException {
		return data.readFloat();
	}

	public final double readDouble() throws IOException {
		return data.readDouble();
	}

	public final String readLine() throws IOException {
		return data.readLine();
	}

	public final String readUTF() throws IOException {
		return data.readUTF();
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return data.skipBytes(n);
	}

}
