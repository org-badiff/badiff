package org.badiff.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FileRandomInput extends InputStream implements RandomInput {
	protected File file;
	protected RandomAccessFile data;
	
	public FileRandomInput(File file) throws IOException {
		this.file = file;
		data = new RandomAccessFile(file, "r");
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
		try {
			return data.getFilePointer();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void seek(long pos) throws IOException {
		data.seek(pos);
	}

	@Override
	public int read() throws IOException {
		return data.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return super.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return data.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return data.skipBytes((int) n);
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

	@Deprecated
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
