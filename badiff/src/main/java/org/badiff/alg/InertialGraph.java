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
import java.util.List;

import org.badiff.Op;
import org.badiff.q.CompactingOpQueue;
import org.badiff.q.ListOpQueue;
import org.badiff.q.OpQueue;

/**
 * {@link Graph} which computes diff path lengths based not on the literal edge count between the origin
 * and a node but instead on the logical byte cost to serialize that edge sequence.  The general premise
 * is that the incremental cost of continuing an {@link Op}'s run is often less than the cost of starting
 * a new {@link Op}.<p>
 * 
 * The result of using a weight based on serialization rather than literal edge length is that, while
 * the sum of the runs of the path may be greater than with {@link EditGraph}, the serialized diff
 * will be smaller.<p>
 * 
 * For example, the difference between "Hello world!" and "Hellish cruel world!" is computed by
 * the {@link InertialGraph} as {@code >4-1+9>7;} and computed by {@link EditGraph} as
 * {@code >2+1>1+8>1-1>7;}.  The {@link InertialGraph} uses a total run length of 21
 * compared with {@link EditGraph}'s run length of 21, but the serialized length of the {@link InertialGraph}'s
 * diff is {@code 16}, versus {@code 21} for the {@link EditGraph}. <p>
 * 
 * The disadvantage of the {@link InertialGraph} compared with the {@link EditGraph} is that
 * it uses more memory to compute, and is slightly slower.  On the other hand, you get better diffs.
 * 
 * @author robin
 *
 */
public class InertialGraph implements Graph {
	/**
	 * The incremental cost of beginning the next operation given the 
	 * current operation.  These costs are based on the hadoop-optimized
	 * defaults from {@link AdjustableInertialGraph}.
	 * 
	 * Each operation requires 1 byte for the operation itself, plus 1 (or more)
	 * bytes for the run length.  Additionally, INSERT has 1 byte for each byte in the run.
	 * 
	 */

	private static final int[][] DEFAULT_TRANSITION_COSTS = new int[][] {
			{1,	1,	1,	1}, // From STOP to...
			{3,	1,	3,	4}, // From DELETE to...
			{2,	2,	1,	3}, // From INSERT to...
			{1,	2,	3,	1}, // From NEXT to...
//           S	D	I	N
	};
	
	protected int cost(byte from, byte to) {
		return DEFAULT_TRANSITION_COSTS[from][to];
	}
	
	protected static final int DELETE = 0;
	protected static final int INSERT = 1;
	protected static final int NEXT = 2;
	
	protected static final int NUM_FIELDS = 3;
	
	protected final short[] cost;
	
	protected final int capacity;
	protected byte[] xval;
	protected byte[] yval;

	/**
	 * Create a new {@link InertialGraph} with the given buffer capacity
	 * @param capacity
	 */
	public InertialGraph(int capacity) {
		if(capacity < 4)
			throw new IllegalArgumentException("capacity must be >= 4");

		this.capacity = capacity;

		cost = new short[NUM_FIELDS * capacity];
	}
	
	@Override
	public void compute(byte[] orig, byte[] target) {
		if((orig.length + 1) * (target.length + 1) > capacity)
			throw new IllegalArgumentException("diff axes exceed graph capacity");

		xval = new byte[orig.length + 1]; System.arraycopy(orig, 0, xval, 1, orig.length);
		yval = new byte[target.length + 1]; System.arraycopy(target, 0, yval, 1, target.length);

		cost[DELETE] = 0;
		cost[INSERT] = 0;
		cost[NEXT] = 0;

		int cdd, cdi, cdn, cid, cii, cin, cnd, cni, cnn;
		cdd = cost(Op.DELETE, Op.DELETE);
		cdi = cost(Op.DELETE, Op.INSERT);
		cdn = cost(Op.DELETE, Op.NEXT);
		cid = cost(Op.INSERT, Op.DELETE);
		cii = cost(Op.INSERT, Op.INSERT);
		cin = cost(Op.INSERT, Op.NEXT);
		cnd = cost(Op.NEXT, Op.DELETE);
		cni = cost(Op.NEXT, Op.INSERT);
		cnn = cost(Op.NEXT, Op.NEXT);
		
		
		int pos;
		for(int y = 0; y < yval.length; y++) {
			for(int x = 0; x < xval.length; x++) {
				if(x == 0 && y == 0)
					continue;

				pos = y * xval.length + x;
				// mark entry costs
				int edc, eic, enc;

				if(x == 0) {
					edc = Short.MAX_VALUE;
					eic = cost[(pos-xval.length)*NUM_FIELDS + INSERT];
					enc = Short.MAX_VALUE;
				} else if(y == 0) {
					edc = cost[(pos-1)*NUM_FIELDS + DELETE];
					eic = Short.MAX_VALUE;
					enc = Short.MAX_VALUE;
				} else {
					edc = cost[(pos-1)*NUM_FIELDS + DELETE];
					eic = cost[(pos-xval.length)*NUM_FIELDS + INSERT];
					if(xval[x] == yval[y])
						enc = cost[(pos - 1 - xval.length)*NUM_FIELDS + NEXT];
					else
						enc = Short.MAX_VALUE;
				}
				
				int cost;

				// compute delete cost
				cost = edc + cdd;
				cost = Math.min(cost, eic + cid);
				cost = Math.min(cost, enc + cnd);
				this.cost[pos*NUM_FIELDS + DELETE] = (short) Math.min(cost, Short.MAX_VALUE);

				// compute insert cost
				cost = eic + cii;
				cost = Math.min(cost, edc + cdi);
				cost = Math.min(cost, enc + cni);
				this.cost[pos*NUM_FIELDS + INSERT] = (short) Math.min(cost, Short.MAX_VALUE);

				// compute next cost
				cost = enc + cnn;
				cost = Math.min(cost, edc + cdn);
				cost = Math.min(cost, eic + cin);
				this.cost[pos*NUM_FIELDS + NEXT] = (short) Math.min(cost, Short.MAX_VALUE);				
			}
		}	
	}

	@Override
	public OpQueue queue() {
		OpQueue rq = new GraphOpQueue();
		List<Op> ops = new ArrayList<Op>();
		for(Op e = rq.poll(); e != null; e = rq.poll())
			ops.add(0, e);
		OpQueue q = new ListOpQueue(ops);
		q = new CompactingOpQueue(q);
		return q;
	}

	protected class GraphOpQueue extends OpQueue {
		protected int pos;
		protected byte prev = Op.STOP;
		protected int x;
		protected int y;

		public GraphOpQueue() {
			pos = xval.length * yval.length - 1;
			x = xval.length-1;
			y = yval.length-1;
		}

		@Override
		protected boolean pull() {
			if(pos == 0)
				return false;

			byte op = -1;
			int cost = Integer.MAX_VALUE;
			if(x > 0 && y > 0 && xval[x] == yval[y]) {
				op = Op.NEXT;
				cost = InertialGraph.this.cost[(pos-1-xval.length)*NUM_FIELDS+NEXT] + cost(Op.NEXT, prev);
			}
			
			if(y > 0 && InertialGraph.this.cost[(pos-xval.length)*NUM_FIELDS+INSERT] + cost(Op.INSERT, prev) < cost) {
				op = Op.INSERT;
				cost = InertialGraph.this.cost[(pos-xval.length)*NUM_FIELDS + INSERT] + cost(Op.INSERT, prev);
			}

			if(x > 0 && InertialGraph.this.cost[(pos-1)*NUM_FIELDS+DELETE] + cost(Op.DELETE, prev) < cost) {
				op = Op.DELETE;
				cost = InertialGraph.this.cost[(pos-1)*NUM_FIELDS+DELETE] + cost(Op.DELETE, prev);
			}

			Op e = null;

			switch(op) {
			case Op.NEXT:
				e = new Op(Op.NEXT, 1, null);
				pos = pos - 1 - xval.length;
				x--;
				y--;
				break;
			case Op.INSERT:
				e = new Op(Op.INSERT, 1, new byte[] {yval[y]});
				pos = pos - xval.length;
				y--;
				break;
			case Op.DELETE:
				e = new Op(Op.DELETE, 1, new byte[] {xval[x]});
				pos = pos - 1;
				x--;
				break;
			}
			
			if(e == null)
				throw new IllegalStateException();

			prepare(e);
			
			prev = e.getOp();

			return true;
		}
	}

}
