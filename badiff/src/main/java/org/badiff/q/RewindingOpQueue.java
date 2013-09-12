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
import java.util.Map;
import java.util.TreeMap;

import org.badiff.Op;

public class RewindingOpQueue extends FilterOpQueue {

	protected int maxkeys;
	protected int minlength;
	protected long lookbehind;
	protected long pos;
	protected TreeMap<Long, byte[]> deletes = new TreeMap<Long, byte[]>();
	
	public RewindingOpQueue(OpQueue source) {
		this(source, 1024*1024);
	}
	
	public RewindingOpQueue(OpQueue source, long lookbehind) {
		this(source, lookbehind, 16);
	}
	
	public RewindingOpQueue(OpQueue source, long lookbehind, int minlength) {
		this(source, lookbehind, minlength, 1024);
	}
	
	public RewindingOpQueue(OpQueue source, long lookbehind, int minlength, int maxkeys) {
		super(source);
		this.lookbehind = lookbehind;
		this.minlength = minlength;
		this.maxkeys = maxkeys;
	}

	@Override
	protected boolean pull() {
		if(!require(1))
			return flush();
		
		Op e = filtering.remove(0);
		
		switch(e.getOp()) {
		case Op.NEXT:
			pos += e.getRun();
			deletes.subMap(Long.MIN_VALUE, pos - lookbehind).clear();
			prepare(e);
			return true;

		case Op.DELETE:
			if(e.getData() != null && e.getData().length >= minlength) {
				deletes.put(pos, e.getData());
				deletes.subMap(Long.MIN_VALUE, pos - lookbehind).clear();
				while(deletes.size() > maxkeys)
					deletes.remove(deletes.firstKey());
			}
			pos += e.getRun();
			prepare(e);
			return true;
			
		case Op.INSERT:
			long dpos = -1;
			
			if(e.getData().length >= minlength) {
				for(Map.Entry<Long, byte[]> en : deletes.entrySet()) {
					if(!Arrays.equals(e.getData(), en.getValue()))
						continue;
					dpos = en.getKey();
					break;
				}
			}
			
			if(dpos == -1) {
				prepare(e);
				return true;
			}
			
			prepare(new Op(Op.DELETE, (int) (dpos - pos), null));
			prepare(new Op(Op.NEXT, e.getRun(), null));
			prepare(new Op(Op.DELETE, (int)(-(dpos - pos) - e.getRun()), null));
			return true;
		}
		
		// strange switch fallthrough?
		return false;
	}
	
}
