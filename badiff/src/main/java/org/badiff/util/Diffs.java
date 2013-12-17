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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.badiff.Applyable;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.io.RandomInputStream;
import org.badiff.io.RuntimeIOException;
import org.badiff.q.OpQueue;
import org.badiff.q.ReplaceOpQueue;
import org.badiff.q.StreamChunkingOpQueue;

/**
 * Utility methods for computing and using differences
 * @author robin
 *
 */
public class Diffs {
	
	public static OpQueue queue(byte[] orig, byte[] target) {
		return new ReplaceOpQueue(orig, target);
	}
	
	public static OpQueue queue(InputStream orig, InputStream target) {
		return new StreamChunkingOpQueue(orig, target);
	}
	
	public static OpQueue improved(OpQueue q) {
		return BadiffFileDiff.PIPE.from(q).outlet();
	}
	
	public static byte[] apply(Applyable a, byte[] orig) {
		InputStream in = new RandomInputStream(orig);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			a.apply(in, out);
			if(in.available() > 0)
				throw new IOException("Not all byts consumed from byte[]");
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
		return out.toByteArray();
	}
	
	public static void apply(Applyable a, File orig, File target) throws IOException {
		InputStream in = new RandomInputStream(orig);
		try {
			FileOutputStream out = new FileOutputStream(target);
			try {
				a.apply(in, out);
			} finally {
				out.close();
			}
		} finally {
			if(in.available() > 0)
				throw new IOException("Not all input data consumed:" + orig);
			in.close();
		}
	}

	private Diffs() {
	}

}
