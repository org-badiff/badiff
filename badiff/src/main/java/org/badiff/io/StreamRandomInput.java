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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamRandomInput implements RandomInput {
	
	protected PushbufferInputStream pin;
	protected DataInputStream din;

	public StreamRandomInput(InputStream in, int size) {
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
	public long skip(long count) throws IOException {
		return pin.skip(count);
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

	@Override
	public int available() throws IOException {
		return pin.available();
	}
	
	@Override
	public void close() throws IOException {
		pin.close();
	}
}
