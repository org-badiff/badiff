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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.badiff.Applyable;
import org.badiff.Diff;
import org.badiff.Op;

/**
 * A unidirectional double-buffering queue of {@link Op}.
 * Contains internally two {@link Deque} of {@link Op}: {@link #prepared}
 * and {@link #pending}.  Calls to {@link #poll()} draw from {@link #prepared}
 * unless it is empty, in which case they call {@link #pull()} to move
 * an element from {@link #pending} to {@link #prepared}.<p>
 * 
 * {@link OpQueue} also implements {@link Iterator}.  Don't mix the {@link #poll()} and {@link #offer(Op)}
 * methods with the {@link Iterator} methods.<p>
 * 
 * {@link OpQueue} instances are <b>lazy sequences</b>, and will instantiate
 * or process their contents only in response to calls to {@link #poll()}.
 * @author robin
 *
 */
public class OpQueue implements Applyable, Iterator<Op> {

	/**
	 * The next {@link Op} to be returned by the iterator.
	 */
	protected Op iterNext;
	/**
	 * The {@link Op}s which are prepared to be {@link #poll()}ed
	 */
	private Deque<Op> prepared = new ArrayDeque<Op>();

	/**
	 * Draw the next {@link Op} from this {@link OpQueue}, returning
	 * null if this {@link OpQueue} is empty.
	 * @return
	 */
	public Op poll() {
		Op e = prepared.pollFirst();
		if(e == null) {
			pull();
			e = prepared.pollFirst();
		}
		return e;
	}
	
	/**
	 * Place a {@link Op} in {@link #pending}
	 * @param e
	 * @return
	 */
	public boolean offer(Op e) {
		return prepared.offerLast(e);
	}
	
	public void drain() {
		for(Op e = poll(); e != null; e = poll())
			;
	}
	
	/**
	 * Drain all the {@link Op}s in this object to a {@link List}
	 * @param c
	 * @return
	 */
	public <T extends List<Op>> T drainTo(T c) {
		for(Op e = poll(); e != null; e = poll())
			c.add(e);
		return c;
	}
	
	/**
	 * Drain all the {@link Op}s in this object to another {@link OpQueue}
	 * @param q
	 * @return
	 */
	public <T extends OpQueue> T drainTo(T q) {
		for(Op e = poll(); e != null; e = poll())
			q.offer(e);
		return q;
	}
	
	/**
	 * Overwrite the argument {@link Diff} with the remaining {@link Op}s in this object.
	 * Calls {@link Diff#store(Iterator)} with this object as the argument.
	 * @param diff
	 * @return
	 * @throws IOException
	 */
	public <T extends Diff> T drainTo(T diff) throws IOException {
		diff.store(this);
		return diff;
	}
	
	/**
	 * Called when a {@link Op} should be moved from {@link #pending} to {@link #prepared}.
	 * Override this method to provide lazy sequences that populate {@link #pending} on demand.
	 */
	protected boolean pull() {
		return false;
	}
	
	/**
	 * Call to add an element to the queue of prepared elements
	 * @param e
	 */
	protected void prepare(Op e) {
		prepared.offerLast(e);
	}
	
	@Override
	public void apply(InputStream orig, OutputStream target)
			throws IOException {
		for(Op e = poll(); e != null; e = poll())
			e.apply(orig, target);
	}

	@Override
	public boolean hasNext() {
		if(iterNext == null)
			iterNext = poll();
		return iterNext != null;
	}

	@Override
	public Op next() {
		if(iterNext == null)
			iterNext = poll();
		try {
			return iterNext;
		} finally {
			iterNext = null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
