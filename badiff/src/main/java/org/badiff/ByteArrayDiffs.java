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
package org.badiff;

import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.Serialization;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.UndoOpQueue;
import org.badiff.util.Diffs;
import org.badiff.util.Serials;

/**
 * Utilities for dealing with {@link Diff}s as {@code byte[]}.
 * 
 * @author robin
 *
 */
public class ByteArrayDiffs {

	/**
	 * The {@link Serialization} to use for persistence
	 */
	protected Serialization serial;
	
	/**
	 * Create a new {@link ByteArrayDiffs} utilities instance
	 */
	public ByteArrayDiffs() {
		this(DefaultSerialization.getInstance());
	}
	
	/**
	 * Create a new {@link ByteArrayDiffs} utilities instance with a specified {@link Serialization}
	 * @param serial
	 */
	public ByteArrayDiffs(Serialization serial) {
		this.serial = serial;
	}
	
	/**
	 * Compute and return a diff between {@code orig} and {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public byte[] diff(byte[] orig, byte[] target) {
		MemoryDiff md = new MemoryDiff();
		md.store(Diffs.improved(Diffs.queue(orig, target)));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}
	
	/**
	 * Apply {@code diff} to {@code orig} and return the result
	 * @param orig
	 * @param diff
	 * @return
	 */
	public byte[] apply(byte[] orig, byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		return Diffs.apply(md, orig);
	}

	/**
	 * Compute and return a one-way (unidirectional) diff from {@code orig} to {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public byte[] udiff(byte[] orig, byte[] target) {
		MemoryDiff md = new MemoryDiff();
		md.store(new OneWayOpQueue(Diffs.improved(Diffs.queue(orig, target))));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}
	
	/**
	 * Apply the inverse of {@code diff} to {@code target} and return the result
	 * @param target
	 * @param diff
	 * @return
	 */
	public byte[] undo(byte[] target, byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		return Diffs.apply(new UndoOpQueue(md.queue()), target);
	}

	/**
	 * Compute and return a one-way (unidirectional) diff given any diff
	 * @param diff
	 * @return
	 */
	public byte[] udiff(byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		md.store(new OneWayOpQueue(md.queue()));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}
	
	/**
	 * Compute and return an "undo" diff from a two-way diff
	 * @param diff
	 * @return
	 */
	public byte[] undo(byte[] diff) {
		MemoryDiff md = Serials.deserialize(serial, MemoryDiff.class, diff);
		md.store(new UndoOpQueue(md.queue()));
		return Serials.serialize(serial, MemoryDiff.class, md);
	}

}
