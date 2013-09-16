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
	 * current operation.  These costs are based on the actual serialization
	 * output.
	 * 
	 * Each operation requires 1 byte for the operation itself, plus 1 (or more)
	 * bytes for the run length.  Additionally, INSERT has 1 byte for each byte in the run.
	 * 
	 * DELETE takes 2 bytes to start (op, run) and 0 bytes to continue
	 * INSERT takes 3 bytes to start (op, run, data) and 1 byte to continue
	 * NEXT takes 1 bytes to start (op, run) and 0 bytes to continue
	 * 
	 */

	@SuppressWarnings("unused")
	private static final int[][] DEFAULT_TRANSITION_COSTS = new int[][] {
			{0, 2, 3, 1}, // From STOP to...
			{0, 0, 3, 1}, // From DELETE to...
			{0, 2, 1, 1}, // From INSERT to...
			{0, 2, 3, 0}, // From NEXT to...
//           S  D  I  N
	};

	protected int cost(byte from, byte to) {
		if(from == to)
			return (to == Op.INSERT) ? 1 : 0;
		if(to == Op.DELETE)
			return 2;
		if(to == Op.INSERT)
			return 3;
		if(to == Op.NEXT)
			return 1;
		return 0;
	}
	
//	protected final int[][] transitionCosts = DEFAULT_TRANSITION_COSTS;
	
	protected static final int ENTER_DELETE = 0;
	protected static final int ENTER_INSERT = 1;
	protected static final int ENTER_NEXT = 2;
	protected static final int LEAVE_DELETE = 3;
	protected static final int LEAVE_INSERT = 4;
	protected static final int LEAVE_NEXT = 5;
	
	protected final short[] cost;
	
//	protected final short[] enterDeleteCost, enterInsertCost, enterNextCost; // Entry costs for this position
//	protected final short[] leaveDeleteCost, leaveInsertCost, leaveNextCost; // Exit costs for this position

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

		cost = new short[6 * capacity];
		
//		enterDeleteCost = new short[capacity];
//		enterInsertCost = new short[capacity];
//		enterNextCost = new short[capacity];
//		leaveDeleteCost = new short[capacity];
//		leaveInsertCost = new short[capacity];
//		leaveNextCost = new short[capacity];
	}
	
	@Override
	public void compute(byte[] orig, byte[] target) {
		if((orig.length + 1) * (target.length + 1) > capacity)
			throw new IllegalArgumentException("diff axes exceed graph capacity");

		xval = new byte[orig.length + 1]; System.arraycopy(orig, 0, xval, 1, orig.length);
		yval = new byte[target.length + 1]; System.arraycopy(target, 0, yval, 1, target.length);

		int pos = -1;
		for(int y = 0; y < yval.length; y++) {
			for(int x = 0; x < xval.length; x++) {
				pos++;
				if(x == 0 && y == 0) {
//					leaveDeleteCost[pos] = (short) cost(Op.STOP, Op.DELETE);
//					leaveInsertCost[pos] = (short) cost(Op.STOP, Op.INSERT);
//					leaveNextCost[pos] = (short) cost(Op.STOP, Op.NEXT);
					cost[LEAVE_DELETE] = 2;
					cost[LEAVE_INSERT] = 3;
					cost[LEAVE_NEXT] = 1;
					continue;
				}

				// mark entry costs
				boolean nextable = x > 0 && y > 0 && xval[x] == yval[y];
//				int edc = enterDeleteCost[pos] = (x == 0) ? Short.MAX_VALUE : leaveDeleteCost[pos-1];
//				int eic = enterInsertCost[pos] = (y == 0) ? Short.MAX_VALUE : leaveInsertCost[pos-xval.length];
//				int enc = enterNextCost[pos] = (!nextable) ? Short.MAX_VALUE : leaveNextCost[pos-1-xval.length];

				int edc = cost[pos*6 + ENTER_DELETE] = (x == 0) ? Short.MAX_VALUE : cost[(pos-1)*6 + LEAVE_DELETE];
				int eic = cost[pos*6 + ENTER_INSERT] = (y == 0) ? Short.MAX_VALUE : cost[(pos-xval.length)*6 + LEAVE_INSERT];
				int enc = cost[pos*6 + ENTER_NEXT] = (!nextable) ? Short.MAX_VALUE : cost[(pos - 1 - xval.length)*6 + LEAVE_NEXT];
				
				int cost;

				// compute delete cost
				cost = edc + 0; // appending a delete is free
				cost = Math.min(cost, eic + 2);
				cost = Math.min(cost, enc + 2);
//				leaveDeleteCost[pos] = (short) Math.min(cost, Short.MAX_VALUE);
				this.cost[pos*6 + LEAVE_DELETE] = (short) Math.min(cost, Short.MAX_VALUE);

				// compute insert cost
				cost = eic + 1; // appending an insert costs 1
				cost = Math.min(cost, edc + 3);
				cost = Math.min(cost, enc + 3);
//				leaveInsertCost[pos] = (short) Math.min(cost, Short.MAX_VALUE);
				this.cost[pos*6 + LEAVE_INSERT] = (short) Math.min(cost, Short.MAX_VALUE);

				// compute next cost
				cost = enc + 0;
				cost = Math.min(cost, edc + 1);
				cost = Math.min(cost, eic + 1);
				this.cost[pos*6 + LEAVE_NEXT] = (short) Math.min(cost, Short.MAX_VALUE);				
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

		public GraphOpQueue() {
			pos = xval.length * yval.length - 1;
		}

		@Override
		protected boolean pull() {
			if(pos == 0)
				return false;

			byte op = Op.NEXT;
			int cost = InertialGraph.this.cost[pos*6+ENTER_NEXT] + cost(Op.NEXT, prev);

			if(InertialGraph.this.cost[pos*6+ENTER_INSERT] + cost(Op.INSERT, prev) < cost) {
				op = Op.INSERT;
				cost = InertialGraph.this.cost[pos*6 + ENTER_INSERT] + cost(Op.INSERT, prev);
			}

			if(InertialGraph.this.cost[pos*6+ENTER_DELETE] + cost(Op.DELETE, prev) < cost) {
				op = Op.DELETE;
				cost = InertialGraph.this.cost[pos*6+ENTER_DELETE] + cost(Op.DELETE, prev);
			}

			Op e = null;

			switch(op) {
			case Op.NEXT:
				e = new Op(Op.NEXT, 1, null);
				pos = pos - 1 - xval.length;
				break;
			case Op.INSERT:
				e = new Op(Op.INSERT, 1, new byte[] {yval[pos / xval.length]});
				pos = pos - xval.length;
				break;
			case Op.DELETE:
				e = new Op(Op.DELETE, 1, new byte[] {xval[pos % xval.length]});
				pos = pos - 1;
				break;
			}

			prepare(e);
			
			prev = e.getOp();

			return true;
		}
	}

}
