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
package org.badiff.q;

import java.io.IOException;
import java.util.Arrays;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.RandomInput;
import org.badiff.io.RuntimeIOException;

/**
 * {@link OpQueue} that lazily reads from two input streams (original and target)
 * and produces chunked output similar to {@link ChunkingOpQueue}.
 * @author robin
 *
 */
public class RandomChunkingOpQueue extends OpQueue {
	protected RandomInput orig;
	protected RandomInput target;
	protected int chunk;
	
	/**
	 * Create a {@link RandomChunkingOpQueue} with the default chunk size
	 * @param orig
	 * @param target
	 */
	public RandomChunkingOpQueue(RandomInput orig, RandomInput target) {
		this(orig, target, Diff.DEFAULT_CHUNK);
	}
	
	/**
	 * Create an {@link OpQueue} lazily populated with alternating chunks of data read
	 * from the streams.
	 * @param orig The source of {@link Op#DELETE} chunks
	 * @param target The source of {@link Op#INSERT} chunks
	 * @param chunk
	 */
	public RandomChunkingOpQueue(RandomInput orig, RandomInput target, int chunk) {
		this.orig = orig;
		this.target = target;
		this.chunk = chunk;
	}
	
	@Override
	protected boolean pull() {
		/*
		 * Lazily offer new chunks if available
		 */
		byte[] obuf = readChunk(orig);
		byte[] tbuf = readChunk(target);

		if(obuf != null)
			prepare(new Op(Op.DELETE, obuf.length, obuf));
		if(tbuf != null)
			prepare(new Op(Op.INSERT, tbuf.length, tbuf));
		
		return obuf != null || tbuf != null;
	}

	/**
	 * Read a chunk from the {@link RandomInput}
	 * @param in
	 * @return
	 */
	protected byte[] readChunk(RandomInput in) {
		try {
			byte[] buf = new byte[chunk];
			int r = in.read(buf);
			if(r == -1)
				return null;
			if(r == chunk)
				return buf;
			return Arrays.copyOf(buf, r);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}
	
}
