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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class RandomInputStream extends InputStream implements Random {
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
	
	public RandomInputStream(InputStream in, int bufSize) {
		this(new StreamRandomInput(in, bufSize));
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

	@Override
	public long first() {
		return in.first();
	}

	@Override
	public long last() {
		return in.last();
	}

	@Override
	public long position() {
		return in.position();
	}

	@Override
	public void seek(long pos) throws IOException {
		in.seek(pos);
	}

}
