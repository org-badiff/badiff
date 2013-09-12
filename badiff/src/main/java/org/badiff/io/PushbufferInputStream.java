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
