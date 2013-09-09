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

import java.io.IOException;
import java.util.Iterator;

import org.badiff.q.OpQueue;

/**
 * A byte-level difference between two inputs.  Can be applied to streams
 * via {@link Applyable}.  {@link Diff} is a <b>re-usable</b> instance of {@link Applyable}.
 * @author robin
 *
 */
public interface Diff extends Applyable {
	/**
	 * The default size of a chunk for operations which chunk their input
	 */
	public final int DEFAULT_CHUNK = 1024;
	
	/**
	 * Overwrite this {@link Diff}'s operations with the operations from the
	 * argument {@link Iterator}
	 * @param ops
	 * @throws IOException
	 */
	public void store(Iterator<Op> ops) throws IOException;
	
	/**
	 * Return a copy of this {@link Diff}'s operations.  This copy may
	 * be {@link OpQueue#poll()}'d from but not {@link OpQueue#offer(Op)}'d to.
	 * @return
	 * @throws IOException
	 */
	public OpQueue queue() throws IOException;
}
