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

import java.util.List;
import java.util.ListIterator;

import org.badiff.Op;
import org.badiff.alg.Graph;

/**
 * {@link OpQueue} that replaces ({@link Op#DELETE},{@link Op#INSERT}) pairs
 * with their {@link Graph}'d equivalents
 * @author robin
 *
 */
public class GraphOpQueue extends FilterOpQueue {
	
	protected Graph graph;

	public GraphOpQueue(OpQueue source, int chunk) {
		this(source, new Graph((chunk+1) * (chunk+1)));
	}
	
	public GraphOpQueue(OpQueue source, Graph graph) {
		super(source);
		this.graph = graph;
	}

	@Override
	protected boolean pull() {
		if(!require(2))
			return flush();
		
		Op delete = null;
		Op insert = null;
		
		if(filtering.get(0).getOp() == Op.DELETE && filtering.get(1).getOp() == Op.INSERT) {
			delete = filtering.get(0);
			insert = filtering.get(1);
		} else if(filtering.get(0).getOp() == Op.INSERT && filtering.get(1).getOp() == Op.DELETE) {
			delete = filtering.get(1);
			insert = filtering.get(0);
		} else
			return flush();
		
		filtering.remove(1);
		filtering.remove(0);
		
		graph.compute(delete.getData(), insert.getData());
		List<Op> rlist = graph.rlist();
		
		ListIterator<Op> oi = rlist.listIterator(rlist.size());
		while(oi.hasPrevious()) {
			prepare(oi.previous());
		}
		
		return true;
	}
		
	
}
