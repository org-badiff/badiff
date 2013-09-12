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
package org.badiff.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.badiff.Op;
import org.badiff.q.CompactingOpQueue;
import org.badiff.q.ListOpQueue;
import org.badiff.q.OpQueue;

public class InnertialGraph implements Graph {

	private boolean[] nextable;
	private short[] enterDeleteCost, enterInsertCost, enterNextCost;
	private short[] leaveDeleteCost, leaveInsertCost, leaveNextCost;

	private int capacity;
	private byte[] xval;
	private byte[] yval;

	public InnertialGraph(int capacity) {
		if(capacity < 4)
			throw new IllegalArgumentException("capacity must be >= 4");

		this.capacity = capacity;

		nextable = new boolean[capacity];
		enterDeleteCost = new short[capacity];
		enterInsertCost = new short[capacity];
		enterNextCost = new short[capacity];
		leaveDeleteCost = new short[capacity];
		leaveInsertCost = new short[capacity];
		leaveNextCost = new short[capacity];
		
		leaveDeleteCost[0] = 2;
		leaveInsertCost[0] = 3;
		leaveNextCost[0] = 2;
	}

	public void compute(byte[] orig, byte[] target) {
		if((orig.length + 1) * (target.length + 1) > capacity)
			throw new IllegalArgumentException("diff axes exceed graph capacity");

		xval = new byte[orig.length + 1]; System.arraycopy(orig, 0, xval, 1, orig.length);
		yval = new byte[target.length + 1]; System.arraycopy(target, 0, yval, 1, target.length);

		int pos = -1;
		for(int y = 0; y < yval.length; y++) {
			for(int x = 0; x < xval.length; x++) {
				pos++;
				if(x == 0 && y == 0)
					continue;

				// mark entry costs
				nextable[pos] = x > 0 && y > 0 && xval[x] == yval[y];
				enterDeleteCost[pos] = (x == 0) ? Short.MAX_VALUE : leaveDeleteCost[pos-1];
				enterInsertCost[pos] = (y == 0) ? Short.MAX_VALUE : leaveInsertCost[pos-xval.length];
				enterNextCost[pos] = (x == 0 || y == 0) ? Short.MAX_VALUE : leaveNextCost[pos-1-xval.length];

				computeDeleteCost(pos);
				computeInsertCost(pos);
				computeNextCost(pos);
			}
		}	
	}

	private void computeDeleteCost(int pos) {
		int cost;
	
		cost = enterDeleteCost[pos]; // appending a delete is free
	
		if(enterInsertCost[pos] + 2 < cost) { // costs 3 to switch from insert to delete
			cost = enterInsertCost[pos] + 3;
		}
	
		if(enterNextCost[pos] + 2 < cost) { // costs 3 to switch from next to delete
			cost = enterNextCost[pos] + 3;
		}
	
		leaveDeleteCost[pos] = (short) cost;
	}

	private void computeInsertCost(int pos) {
		int cost;
	
		cost = enterInsertCost[pos] + 1; // appending an insert costs 1
	
		if(enterDeleteCost[pos] + 3 < cost) { // costs 3 to switch from delete to insert
			cost = enterDeleteCost[pos] + 3;
		}
	
		if(enterNextCost[pos] + 3 < cost) { // costs 3 to switch from next to insert
			cost = enterNextCost[pos] + 3;
		}
	
		leaveInsertCost[pos] = (short) cost;
	}

	private void computeNextCost(int pos) {
		if(!nextable[pos]) {
			leaveNextCost[pos] = Short.MAX_VALUE;
			return;
		}
	
		int cost;
	
		cost = enterNextCost[pos]; // appending a next is free
	
		if(enterDeleteCost[pos] + 2 < cost) { // costs 2 to switch from delete to next
			cost = enterDeleteCost[pos] + 2;
		}
	
		if(enterInsertCost[pos] + 2 < cost) { // costs 2 to switch from insert to next
			cost = enterInsertCost[pos] + 2;
		}
	
		leaveNextCost[pos] = (short) cost;
	}

	public OpQueue queue() {
		OpQueue rq = new GraphOpQueue();
		List<Op> ops = new ArrayList<Op>();
		for(Op e = rq.poll(); e != null; e = rq.poll())
			ops.add(0, e);
		OpQueue q = new ListOpQueue(ops);
		q = new CompactingOpQueue(q);
		return q;
	}

	private class GraphOpQueue extends OpQueue {
		private int pos;


		public GraphOpQueue() {
			pos = xval.length * yval.length - 1;

			int cost = leaveNextCost[pos];
			byte op = Op.NEXT;

			if(leaveInsertCost[pos] < cost) {
				cost = leaveInsertCost[pos];
				op = Op.INSERT;
			}

			if(leaveDeleteCost[pos] < cost) {
				cost = leaveDeleteCost[pos];
				op = Op.DELETE;
			}

			switch(op) {
			case Op.NEXT:
				prepare(new Op(Op.NEXT, 1, null));
				break;
			case Op.INSERT:
				prepare(new Op(Op.INSERT, 1, new byte[] {yval[pos / xval.length]}));
				break;
			case Op.DELETE:
				prepare(new Op(Op.DELETE, 1, new byte[] {xval[pos % xval.length]}));
				break;
			}
		}

		@Override
		protected boolean pull() {
			if(pos == 0)
				return false;

			byte op = Op.NEXT;
			int cost = enterNextCost[pos];

			if(enterInsertCost[pos] < cost) {
				op = Op.INSERT;
				cost = enterInsertCost[pos];
			}

			if(enterDeleteCost[pos] < cost) {
				op = Op.DELETE;
				cost = enterDeleteCost[pos];
			}

			Op e = null;

			switch(op) {
			case Op.NEXT:
				pos = pos - 1 - xval.length;
				e = new Op(Op.NEXT, 1, null);
				break;
			case Op.INSERT:
				pos = pos - xval.length;
				e = new Op(Op.INSERT, 1, new byte[] {yval[pos / xval.length]});
				break;
			case Op.DELETE:
				pos = pos - 1;
				e = new Op(Op.DELETE, 1, new byte[] {xval[pos % xval.length]});
				break;
			}

			if(pos > 0)
				prepare(e);

			return true;
		}
	}

}
