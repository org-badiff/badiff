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

import java.util.ArrayDeque;
import java.util.Deque;

import org.badiff.Op;

/**
 * {@link OpQueue} that draws its {@link Op} elements
 * from the head of a queue of other {@link OpQueue} instances,
 * discarding the head when it is emptied.  Lazy.
 * @author robin
 *
 */
public class ChainOpQueue extends OpQueue {
	
	/**
	 * The queue of {@link OpQueue} to draw elements from
	 */
	protected Deque<OpQueue> chain = new ArrayDeque<OpQueue>();

	/**
	 * Create a new {@link OpQueue} that draws its elements from the argument {@link OpQueue}s
	 * in a lazy manner, emptying each {@link OpQueue} before drawing any from the next.
	 * @param links
	 */
	public ChainOpQueue(OpQueue... links) {
		for(OpQueue q : links)
			offer(q);
	}
	
	/**
	 * Returns the queue of {@link OpQueue} used to draw elements from
	 * @return
	 */
	public Deque<OpQueue> getChain() {
		return chain;
	}
	
	/**
	 * Add another {@link OpQueue} to the chain
	 * @param q
	 * @return
	 */
	public boolean offer(OpQueue q) {
		return chain.offerLast(q);
	}
	
	@Override
	public boolean offer(Op e) {
		if(chain.size() == 0)
			chain.offerLast(new OpQueue());
		return chain.peekLast().offer(e);
	}
	
	@Override
	protected boolean pull() {
		/*
		 * If there are no pending DiffOp's, then try to draw one from
		 * the head of the chain until either there is a pending DiffOp
		 * or the chain is empty.
		 */
		while(chain.size() > 0) {
			Op e = chain.peekFirst().poll();
			if(e == null) {
				chain.pollFirst();
				continue;
			}
			prepare(e);
			return true;
		}
		return false;
	}

}
