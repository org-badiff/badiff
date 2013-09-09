/**
 * badiff - byte array diff - fast pure-java byte-level diffing
 * 
 * Copyright (c) 2013, Robin Kirkman All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3) Neither the name of the badiff nor the names of its contributors may be 
 *    used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.badiff.io;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferRandomInput implements RandomInput {
	
	protected ByteBuffer buf;
	
	public ByteBufferRandomInput(ByteBuffer buf) {
		this.buf = buf;
	}
	
	public ByteBufferRandomInput(byte[] buf) {
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
