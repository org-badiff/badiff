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

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.alg.EditGraph;

/**
 * {@link OpQueue} that lazily chunks pairs of pending
 * DiffOp's with the types ({@link Op#DELETE},{@link Op#INSERT}).
 * Chunking means removing the two pending operations and replacing them
 * with alternating {@link Op#DELETE} and {@link Op#INSERT} operations
 * whose {@link Op#getRun()} length is no greater than the chunk size.<p>
 * 
 * Chunking is used primarily to pre-process input to other algorithms,
 * such as {@link EditGraph}, into manageable sizes.
 * @author robin
 *
 */
public class ChunkingOpQueue extends FilterOpQueue {

	/**
	 * The chunk size
	 */
	protected int chunk;
	
	/**
	 * Create a {@link ChunkingOpQueue} with a default chunk size
	 * @param source
	 */
	public ChunkingOpQueue(OpQueue source) {
		this(source, Diff.DEFAULT_CHUNK);
	}
	
	/**
	 * Create a {@link ChunkingOpQueue} with a specified chunk size
	 * @param source
	 * @param chunk
	 */
	public ChunkingOpQueue(OpQueue source, int chunk) {
		super(source);
		this.chunk = chunk;
	}

	@Override
	protected boolean pull() {
		/*
		 * Look for a (DELETE,INSERT) pair at the head of the pending queue
		 */
		if(!require(2))
			return flush();
		
		if(filtering.get(0).getOp() != Op.DELETE || filtering.get(1).getOp() != Op.INSERT)
			return flush();
		
		Op delete = filtering.get(0);
		Op insert = filtering.get(1);
		
		filtering.remove(1);
		filtering.remove(0);
		
		/*
		 * Chunk the delete and insert
		 */
		
		byte[] ddata = delete.getData();
		byte[] idata = insert.getData();
		
		int dpos = 0;
		int ipos = 0;
		
		while(dpos < ddata.length || ipos < idata.length) {
			if(dpos < ddata.length) {
				byte[] data = new byte[Math.min(chunk, ddata.length - dpos)];
				System.arraycopy(ddata, dpos, data, 0, data.length);
				prepare(new Op(Op.DELETE, data.length, data));
				dpos += data.length;
			}
			if(ipos < idata.length) {
				byte[] data = new byte[Math.min(chunk, idata.length - ipos)];
				System.arraycopy(idata, ipos, data, 0, data.length);
				prepare(new Op(Op.INSERT, data.length, data));
				ipos += data.length;
			}
		}
		
		return true;
	}

}
