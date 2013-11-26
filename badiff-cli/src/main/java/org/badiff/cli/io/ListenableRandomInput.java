package org.badiff.cli.io;

import java.io.IOException;
import java.util.Arrays;

import org.badiff.io.RandomInput;

public class ListenableRandomInput implements RandomInput {
	protected RandomInput input;
	protected RandomInputListener[] listeners = new RandomInputListener[0];

	public ListenableRandomInput(RandomInput input) {
		this.input = input;
	}
	
	public void addListener(RandomInputListener l) {
		RandomInputListener[] ll = Arrays.copyOf(listeners, listeners.length + 1);
		ll[ll.length - 1] = l;
		listeners = ll;
	}
	
	public void removeListener(RandomInputListener l) {
		int i = listeners.length - 1;
		while(i >= 0 && listeners[i] != l)
			i--;
		if(i < 0)
			return;
		RandomInputListener[] ll = Arrays.copyOf(listeners, listeners.length - 1);
		System.arraycopy(listeners, i + 1, ll, i, listeners.length - (i + 1));
		listeners = ll;
	}

	protected void moved() {
		for(int i = listeners.length - 1; i >= 0; i--)
			listeners[i].moved(this);
	}

	public long first() {
		return input.first();
	}

	public int read() throws IOException {
		try { return input.read(); } finally { moved(); }
	}

	public long last() {
		return input.last();
	}

	public int read(byte[] b) throws IOException {
		try { return input.read(b); } finally { moved(); }
	}

	public int read(byte[] b, int off, int len) throws IOException {
		try { return input.read(b, off, len); } finally { moved(); }
	}

	public long position() {
		return input.position();
	}

	public int available() throws IOException {
		return input.available();
	}

	public void close() throws IOException {
		try { input.close(); } finally { moved(); }
	}

	public void seek(long pos) throws IOException {
		try { input.seek(pos); } finally { moved(); }
	}

	public long skip(long count) throws IOException {
		try { return input.skip(count); } finally { moved(); }
	}

	public void readFully(byte[] b) throws IOException {
		try { input.readFully(b); } finally { moved(); }
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		try { input.readFully(b, off, len); } finally { moved(); }
	}

	public int skipBytes(int n) throws IOException {
		try { return input.skipBytes(n); } finally { moved(); }
	}

	public boolean readBoolean() throws IOException {
		try { return input.readBoolean(); } finally { moved(); }
	}

	public byte readByte() throws IOException {
		try { return input.readByte(); } finally { moved(); }
	}

	public int readUnsignedByte() throws IOException {
		try { return input.readUnsignedByte(); } finally { moved(); }
	}

	public short readShort() throws IOException {
		try { return input.readShort(); } finally { moved(); }
	}

	public int readUnsignedShort() throws IOException {
		try { return input.readUnsignedShort(); } finally { moved(); }
	}

	public char readChar() throws IOException {
		try { return input.readChar(); } finally { moved(); }
	}

	public int readInt() throws IOException {
		try { return input.readInt(); } finally { moved(); }
	}

	public long readLong() throws IOException {
		try { return input.readLong(); } finally { moved(); }
	}

	public float readFloat() throws IOException {
		try { return input.readFloat(); } finally { moved(); }
	}

	public double readDouble() throws IOException {
		try { return input.readDouble(); } finally { moved(); }
	}

	public String readLine() throws IOException {
		try { return input.readLine(); } finally { moved(); }
	}

	public String readUTF() throws IOException {
		try { return input.readUTF(); } finally { moved(); }
	}
}
