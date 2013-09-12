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

/**
 * {@link Graph} which computes diff path lengths based not on the literal edge count between the origin
 * and a node but instead on the logical byte cost to serialize that edge sequence.  The general premise
 * is that the incremental cost of continuing an {@link Op}'s run is often less than the cost of starting
 * a new {@link Op}.<p>
 * 
 * The graph assigns costs for each possible {@link Op} transition.  Each {@link Op} takes at least
 * two bytes:<p>
 * 
 * <ul>
 * <li>NEXT costs 2 bytes of any run length
 * <li>DELETE costs 3 bytes of any run length (one byte for a null array)
 * <li>INSERT costs 2 bytes plus the run length
 * </ul>
 * 
 * The result of using a weight based on serialization rather than literal edge length is that, while
 * the sum of the runs of the path may be greater than with {@link EditGraph}, the serialized diff
 * will be smaller.<p>
 * 
 * For example, the difference between "Hello world!" and "Hellish cruel world!" is computed by
 * the {@link InnertialGraph} as {@code [>4, -2, +10, >6]} and computed by {@link EditGraph} as
 * {@code [>2, +1, >1, +8, >1, -1, >7]}.  The {@link InnertialGraph} uses a total run length of 22
 * compared with {@link EditGraph}'s run length of 21, but the serialized length of the {@link InnertialGraph}'s
 * diff is {@code 17}, versus {@code 21} for the {@link EditGraph}. 
 * 
 * @author robin
 *
 */
public class InnertialGraph implements Graph {
	/**
	 * The incremental cost of beginning the next operation given the 
	 * current operation.  These costs are based on the actual serialization
	 * output.
	 * 
	 * Each operation requires 1 byte for the operation itself, plus 1 (or more)
	 * bytes for the run length.  Additionally, INSERT has 1 byte for each byte in the run.
	 * 
	 * DELETE takes 3 bytes to start (op, run, null-array) and 0 bytes to continue
	 * INSERT takes 3 bytes to start (op, run, data) and 1 byte to continue
	 * NEXT takes 2 bytes to start (op, run) and 0 bytes to continue
	 * 
	 */
	private short[][] TRANSITION_COSTS = new short[][] {
			{0, 3, 3, 2}, // From STOP to...
			{0, 0, 3, 2}, // From DELETE to...
			{0, 3, 1, 2}, // From INSERT to...
			{0, 3, 3, 0}, // From NEXT to...
//           S  D  I  N
	};

	private boolean[] nextable; // Whether this position can do NEXT
	private short[] enterDeleteCost, enterInsertCost, enterNextCost; // Entry costs for this position
	private short[] leaveDeleteCost, leaveInsertCost, leaveNextCost; // Exit costs for this position

	private int capacity;
	private byte[] xval;
	private byte[] yval;

	/**
	 * Create a new {@link InnertialGraph} with the given buffer capacity
	 * @param capacity
	 */
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
		
		leaveDeleteCost[0] = TRANSITION_COSTS[Op.STOP][Op.DELETE];
		leaveInsertCost[0] = TRANSITION_COSTS[Op.STOP][Op.INSERT];
		leaveNextCost[0] = TRANSITION_COSTS[Op.STOP][Op.NEXT];
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
	
		cost = enterDeleteCost[pos] + TRANSITION_COSTS[Op.DELETE][Op.DELETE]; // appending a delete is free
	
		if(enterInsertCost[pos] + TRANSITION_COSTS[Op.INSERT][Op.DELETE] < cost) { // costs 3 to switch from insert to delete
			cost = enterInsertCost[pos] + TRANSITION_COSTS[Op.INSERT][Op.DELETE];
		}
	
		if(enterNextCost[pos] + TRANSITION_COSTS[Op.NEXT][Op.DELETE] < cost) { // costs 3 to switch from next to delete
			cost = enterNextCost[pos] + TRANSITION_COSTS[Op.NEXT][Op.DELETE];
		}
	
		leaveDeleteCost[pos] = (short) cost;
	}

	private void computeInsertCost(int pos) {
		int cost;
	
		cost = enterInsertCost[pos] + TRANSITION_COSTS[Op.INSERT][Op.INSERT]; // appending an insert costs 1
	
		if(enterDeleteCost[pos] + TRANSITION_COSTS[Op.DELETE][Op.INSERT] < cost) { // costs 3 to switch from delete to insert
			cost = enterDeleteCost[pos] + TRANSITION_COSTS[Op.DELETE][Op.INSERT];
		}
	
		if(enterNextCost[pos] + TRANSITION_COSTS[Op.NEXT][Op.INSERT] < cost) { // costs 3 to switch from next to insert
			cost = enterNextCost[pos] + TRANSITION_COSTS[Op.NEXT][Op.INSERT];
		}
	
		leaveInsertCost[pos] = (short) cost;
	}

	private void computeNextCost(int pos) {
		if(!nextable[pos]) {
			leaveNextCost[pos] = Short.MAX_VALUE;
			return;
		}
	
		int cost;
	
		cost = enterNextCost[pos] + TRANSITION_COSTS[Op.NEXT][Op.NEXT]; // appending a next is free
	
		if(enterDeleteCost[pos] + TRANSITION_COSTS[Op.DELETE][Op.NEXT] < cost) { // costs 2 to switch from delete to next
			cost = enterDeleteCost[pos] + TRANSITION_COSTS[Op.DELETE][Op.NEXT];
		}
	
		if(enterInsertCost[pos] + TRANSITION_COSTS[Op.INSERT][Op.NEXT] < cost) { // costs 2 to switch from insert to next
			cost = enterInsertCost[pos] + TRANSITION_COSTS[Op.INSERT][Op.NEXT];
		}
	
		leaveNextCost[pos] = (short) cost;
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
