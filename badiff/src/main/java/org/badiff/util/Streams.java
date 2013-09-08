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
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import org.badiff.io.DataInputInputStream;
import org.badiff.io.DataOutputOutputStream;

/**
 * Utility methods for dealing with streams
 * @author robin
 *
 */
public class Streams {
	/**
	 * Copy all data from {@code in} to {@code out} without closing either
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		for(int r = in.read(buf); r != -1; r = in.read(buf)) {
			out.write(buf, 0, r);
			count += r;
		}
		return count;
	}
	
	/**
	 * Copy some data from {@code in} to {@code out} without closing either
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out, long length) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		for(int r = in.read(buf, 0, (int) Math.min(buf.length, length - count)); 
				r != -1; 
				r = in.read(buf, 0, (int) Math.min(buf.length, length - count))) {
			out.write(buf, 0, r);
			count += r;
			if(count == length)
				break;
		}
		return count;
	}
	
	/**
	 * View the {@link ObjectOutput} as an {@link OutputStream} by either casting or wrapping
	 * @param out
	 * @return
	 */
	public static OutputStream asStream(DataOutput out) {
		if(out instanceof OutputStream)
			return (OutputStream) out;
		return new DataOutputOutputStream(out);
	}
	
	/**
	 * View thw {@link ObjectInput} as an {@link InputStream} by either casting or wrapping
	 * @param in
	 * @return
	 */
	public static InputStream asStream(DataInput in) {
		if(in instanceof InputStream)
			return (InputStream) in;
		return new DataInputInputStream(in);
	}
	
	private Streams() {}

}
