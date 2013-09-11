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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.badiff.Op;

/**
 * {@link OpQueue} similar to {@link FilterOpQueue}, but draws its elements
 * from an evaluated {@link Future}{@code <OpQueue>} rather than a direct {@link OpQueue}.
 * @author robin
 *
 */
public class FutureOpQueue extends OpQueue {
	/**
	 * The {@link Future} that will supply an {@link OpQueue} to draw elements from
	 */
	protected Future<OpQueue> source;

	/**
	 * {@link OpQueue} that will draw elements from a {@link Future}{@code <OpQueue>}
	 * @param source
	 */
	public FutureOpQueue(Future<OpQueue> source) {
		this.source = source;
	}

	@Override
	public boolean offer(Op e) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected boolean pull() {
		try {
			Op e;
			boolean pulled = false;
			while((e = source.get().poll()) != null) {
				prepare(e);
				pulled = true;
			}
			return pulled;
		} catch(InterruptedException ie) {
			throw new RuntimeException(ie);
		} catch(ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}
}
