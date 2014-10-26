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
package org.badiff.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.badiff.io.EmptyInputStream;
import org.badiff.io.NoopOutputStream;

/**
 * Utility methods for dealing with streams
 * @author robin
 *
 */
public class Data {
	public static final DataInput NOOP_INPUT = new DataInputStream(new EmptyInputStream());
	public static final DataOutput NOOP_OUT = new DataOutputStream(new NoopOutputStream());
	
	public static long copy(InputStream in, OutputStream out) throws IOException {
		return copy((DataInput) asInput(in), (DataOutput) asOutput(out));
	}
	
	public static long copy(DataInput in, OutputStream out) throws IOException {
		return copy(in, (DataOutput) asOutput(out));
	}
	
	public static long copy(InputStream in, DataOutput out) throws IOException {
		return copy((DataInput) asInput(in), out);
	}
	
	/**
	 * Copy all data from {@code in} to {@code out} without closing either
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static long copy(DataInput in, DataOutput out) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		for(;;) {
			int r = 0;
			try {
				while(r < buf.length) {
					byte b = in.readByte();
					buf[r++] = b;
				}
			} catch(EOFException e) {
			}
			if(r == 0)
				return count;
			out.write(buf, 0, r);
			count += r;
		}
	}
	
	public static long copy(InputStream in, OutputStream out, long length) throws IOException {
		return copy((DataInput) asInput(in), (DataOutput) asOutput(out), length);
	}
	
	public static long copy(DataInput in, OutputStream out, long length) throws IOException {
		return copy(in, (DataOutput) asOutput(out), length);
	}
	
	public static long copy(InputStream in, DataOutput out, long length) throws IOException {
		return copy((DataInput) asInput(in), out, length);
	}
	
	/**
	 * Copy some data from {@code in} to {@code out} without closing either
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static long copy(DataInput in, DataOutput out, long length) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		while(count < length) {
			long max = Math.min(buf.length, length);
			int r = 0;
			try {
				while(r < max) {
					byte b = in.readByte();
					buf[r++] = b;
				}
			} catch(EOFException e) {
			}
			if(r == 0)
				return count;
			out.write(buf, 0, r);
			count += r;
		}
		return count;
	}
	
	public static long skip(InputStream in, long length) throws IOException {
		return skip((DataInput) asInput(in), length);
	}
	
	public static long skip(DataInput in, long length) throws IOException {
		long count = copy(in, NOOP_OUT, length);
		if(count < length)
			throw new EOFException();
		return count;
	}
	
	public static DataInputStream asStream(DataInput in) {
		if(in instanceof DataInputStream)
			return (DataInputStream) in;
		InputStream wrapper = new WrapperInputStream(in);
		return new DataInputStream(wrapper);
	}
	
	public static DataOutputStream asStream(DataOutput out) {
		if(out instanceof DataOutputStream)
			return (DataOutputStream) out;
		OutputStream wrapper = new WrapperOutputStream(out);
		return new DataOutputStream(wrapper);
	}
	
	public static DataInputStream asInput(InputStream in) {
		if(in instanceof DataInputStream)
			return (DataInputStream) in;
		return new DataInputStream(in);
	}
	
	public static DataOutputStream asOutput(OutputStream out) {
		if(out instanceof DataOutputStream)
			return (DataOutputStream) out;
		return new DataOutputStream(out);
	}
	
	private static class WrapperOutputStream extends OutputStream {
		private final DataOutput out;

		private WrapperOutputStream(DataOutput out) {
			this.out = out;
		}

		@Override
		public void write(int b) throws IOException {
			out.writeByte(b);
		}
	}

	private static class WrapperInputStream extends InputStream {
		private final DataInput in;
	
		private WrapperInputStream(DataInput in) {
			this.in = in;
		}
	
		@Override
		public int read() throws IOException {
			try {
				return 0xff & in.readByte();
			} catch(EOFException e) {
				return -1;
			}
		}
	}

	private Data() {}

}
