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

import java.util.Arrays;

import org.badiff.Op;

/**
 * {@link OpQueue} that entirely removes pairs of (INSERT,DELETE) {@link Op}s
 * in the head of its {@link OpQueue#pending} queue if they have the same {@link Op#getData()}.
 * Also coalesces consecutive NEXT {@link Op}s
 * @author robin
 *
 */
public class CoalescingOpQueue extends FilterOpQueue {

	/**
	 * Create a {@link OpQueue} that coalesces pairs of equivalent (INSERT,DELETE).
	 * @param source
	 */
	public CoalescingOpQueue(OpQueue source) {
		super(source);
	}

	@Override
	protected boolean pull() {
		while(require(2)) {
			if(filtering.get(0).getOp() == Op.INSERT && filtering.get(1).getOp() == Op.DELETE) {
				Op insert = filtering.get(0);
				Op delete = filtering.get(1);

				if(Arrays.equals(insert.getData(), delete.getData())) {
					drop(2);
					filtering.add(0, new Op(Op.NEXT, insert.getData().length, null));
					continue;
				}

				// bump the pair into the ready queue
				prepare(filtering.remove(0));
				return true;
			}
			if(filtering.get(0).getOp() == Op.DELETE && filtering.get(1).getOp() == Op.INSERT) {
				Op delete = filtering.get(0);
				Op insert = filtering.get(1);

				if(Arrays.equals(delete.getData(), insert.getData())) {
					drop(2);
					filtering.add(0, new Op(Op.NEXT, insert.getData().length, null));
					continue;
				}

				// bump the pair into the ready queue
				prepare(filtering.remove(0));
				return true;
			}
			if(filtering.get(0).getOp() == Op.NEXT && filtering.get(1).getOp() == Op.NEXT) {
				Op n1 = filtering.remove(0);
				Op n2 = filtering.remove(0);
				filtering.add(0, new Op(Op.NEXT, n1.getRun() + n2.getRun(), null));
				continue;
			}
			prepare(filtering.remove(0));
		}
		return flush();
	}

}
