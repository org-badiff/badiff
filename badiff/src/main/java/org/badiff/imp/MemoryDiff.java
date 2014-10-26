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
package org.badiff.imp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.q.ListOpQueue;
import org.badiff.q.OpQueue;

/**
 * Implementation of {@link Diff} that lives entirely in memory, backed
 * by a {@link List} of {@link Op}.
 * @author robin
 *
 */
public class MemoryDiff implements Diff, Serialized {
	
	protected List<Op> ops = new ArrayList<Op>();

	public MemoryDiff() {}
	
	public MemoryDiff(Iterator<Op> ops) {
		this();
		store(ops);
	}
	
	@Override
	public void apply(DataInput orig, DataOutput target)
			throws IOException {
		for(Op e : ops)
			e.apply(orig, target);
	}

	@Override
	public void store(Iterator<Op> ops) {
		this.ops.clear();
		while(ops.hasNext())
			this.ops.add(ops.next());
	}

	@Override
	public OpQueue queue() {
		return new MemoryOpQueue(ops);
	}

	@Override
	public String toString() {
		return queue().consummerize();
	}
	
	@Override
	public void serialize(Serialization serial, DataOutput out)
			throws IOException {
		for(Op e : ops)
			serial.writeObject(out, Op.class, e);
		serial.writeObject(out, Op.class, new Op(Op.STOP, 1, null));
	}

	@Override
	public void deserialize(Serialization serial, DataInput in)
			throws IOException {
		for(Op e = serial.readObject(in, Op.class); e.getOp() != Op.STOP; e = serial.readObject(in, Op.class))
			ops.add(e);
	}

	private class MemoryOpQueue extends ListOpQueue {
		private MemoryOpQueue(List<Op> ops) {
			super(ops);
		}
	
		@Override
		public boolean offer(Op e) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

}
