package org.badiff.alg;

import java.util.ArrayList;
import java.util.List;

import org.badiff.Op;
import org.badiff.alg.AdjustableInertialGraph.GraphOpQueue;
import org.badiff.q.CompactingOpQueue;
import org.badiff.q.ListOpQueue;
import org.badiff.q.OpQueue;

import static org.badiff.Op.*;

public class AdjustableInertialGraph implements Graph {
//	1	1	1	1	3	1	3	4	2	2	1	3	1	2	3	1
	
	private static final int[][] DEFAULT_TRANSITION_COSTS = new int[][] {
			{1,	1,	1,	1}, // From STOP to...
			{3,	1,	3,	4}, // From DELETE to...
			{2,	2,	1,	3}, // From INSERT to...
			{1,	2,	3,	1}, // From NEXT to...
//           S  D  I  N
	};
	
	protected static final int ENTER_DELETE = 0;
	protected static final int ENTER_INSERT = 1;
	protected static final int ENTER_NEXT = 2;
	protected static final int LEAVE_DELETE = 3;
	protected static final int LEAVE_INSERT = 4;
	protected static final int LEAVE_NEXT = 5;
	
	protected float[] cost;
	
	protected final int capacity;
	protected byte[] xval;
	protected byte[] yval;

	protected float stop_stop = DEFAULT_TRANSITION_COSTS[STOP][STOP];

	protected float stop_delete = DEFAULT_TRANSITION_COSTS[STOP][DELETE];

	protected float stop_insert = DEFAULT_TRANSITION_COSTS[STOP][INSERT];

	protected float stop_next = DEFAULT_TRANSITION_COSTS[STOP][NEXT];

	protected float delete_stop = DEFAULT_TRANSITION_COSTS[DELETE][STOP];

	protected float delete_delete = DEFAULT_TRANSITION_COSTS[DELETE][DELETE];

	protected float delete_insert = DEFAULT_TRANSITION_COSTS[DELETE][INSERT];

	protected float delete_next = DEFAULT_TRANSITION_COSTS[DELETE][NEXT];

	protected float insert_stop = DEFAULT_TRANSITION_COSTS[INSERT][STOP];

	protected float insert_delete = DEFAULT_TRANSITION_COSTS[INSERT][DELETE];

	protected float insert_insert = DEFAULT_TRANSITION_COSTS[INSERT][INSERT];

	protected float insert_next = DEFAULT_TRANSITION_COSTS[INSERT][NEXT];

	protected float next_stop = DEFAULT_TRANSITION_COSTS[NEXT][STOP];

	protected float next_delete = DEFAULT_TRANSITION_COSTS[NEXT][DELETE];

	protected float next_insert = DEFAULT_TRANSITION_COSTS[NEXT][INSERT];

	protected float next_next = DEFAULT_TRANSITION_COSTS[NEXT][NEXT];

	/**
	 * Create a new {@link AdjustableInertialGraph} with the given buffer capacity
	 * @param capacity
	 */
	public AdjustableInertialGraph(int capacity) {
		if(capacity < 4)
			throw new IllegalArgumentException("capacity must be >= 4");

		this.capacity = capacity;

		cost = new float[6 * capacity];
		
	}
	
	public float getCost(byte from, byte to) {
		switch(from) {
		case STOP:
			switch(to) {
			case STOP: return stop_stop;
			case DELETE: return stop_delete;
			case INSERT: return stop_insert;
			case NEXT: return stop_next;
			}
		case DELETE:
			switch(to) {
			case STOP: return delete_stop;
			case DELETE: return delete_delete;
			case INSERT: return delete_insert;
			case NEXT: return delete_next;
			}
		case INSERT:
			switch(to) {
			case STOP: return insert_stop;
			case DELETE: return insert_delete;
			case INSERT: return insert_insert;
			case NEXT: return insert_next;
			}
		case NEXT:
			switch(to) {
			case STOP: return next_stop;
			case DELETE: return next_delete;
			case INSERT: return next_insert;
			case NEXT: return next_next;
			}
		}
		throw new IllegalArgumentException("from:" + from + " to:" + to);
	}

	public void setCost(byte from, byte to, float cost) {
		switch(from) {
		case STOP:
			switch(to) {
			case STOP: stop_stop = cost; return;
			case DELETE: stop_delete = cost; return;
			case INSERT: stop_insert = cost; return;
			case NEXT: stop_next = cost; return;
			}
		case DELETE:
			switch(to) {
			case STOP: delete_stop = cost; return;
			case DELETE: delete_delete = cost; return;
			case INSERT: delete_insert = cost; return;
			case NEXT: delete_next = cost; return;
			}
		case INSERT:
			switch(to) {
			case STOP: insert_stop = cost; return;
			case DELETE: insert_delete = cost; return;
			case INSERT: insert_insert = cost; return;
			case NEXT: insert_next = cost; return;
			}
		case NEXT:
			switch(to) {
			case STOP: next_stop = cost; return;
			case DELETE: next_delete = cost; return;
			case INSERT: next_insert = cost; return;
			case NEXT: next_next = cost; return;
			}
		}
		throw new IllegalArgumentException("from:" + from + " to:" + to);
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
					cost[LEAVE_DELETE] = stop_delete;
					cost[LEAVE_INSERT] = stop_insert;
					cost[LEAVE_NEXT] = stop_next;
					continue;
				}

				// mark entry costs
				boolean nextable = x > 0 && y > 0 && xval[x] == yval[y];

				float edc = cost[pos*6 + ENTER_DELETE] = (x == 0) ? Short.MAX_VALUE : cost[(pos-1)*6 + LEAVE_DELETE];
				float eic = cost[pos*6 + ENTER_INSERT] = (y == 0) ? Short.MAX_VALUE : cost[(pos-xval.length)*6 + LEAVE_INSERT];
				float enc = cost[pos*6 + ENTER_NEXT] = (!nextable) ? Short.MAX_VALUE : cost[(pos - 1 - xval.length)*6 + LEAVE_NEXT];
				
				float cost;

				// compute delete cost
				cost = edc + delete_delete;
				cost = Math.min(cost, eic + insert_delete);
				cost = Math.min(cost, enc + next_delete);
				this.cost[pos*6 + LEAVE_DELETE] = (float) Math.min(cost, Short.MAX_VALUE);

				// compute insert cost
				cost = eic + insert_insert; // appending an insert costs 1
				cost = Math.min(cost, edc + delete_insert);
				cost = Math.min(cost, enc + next_insert);
				this.cost[pos*6 + LEAVE_INSERT] = (float) Math.min(cost, Short.MAX_VALUE);

				// compute next cost
				cost = enc + next_next;
				cost = Math.min(cost, edc + delete_next);
				cost = Math.min(cost, eic + insert_next);
				this.cost[pos*6 + LEAVE_NEXT] = (float) Math.min(cost, Short.MAX_VALUE);				
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
			float cost = AdjustableInertialGraph.this.cost[pos*6+ENTER_NEXT] + getCost(Op.NEXT, prev);

			if(AdjustableInertialGraph.this.cost[pos*6+ENTER_INSERT] + getCost(Op.INSERT, prev) < cost) {
				op = Op.INSERT;
				cost = AdjustableInertialGraph.this.cost[pos*6 + ENTER_INSERT] + getCost(Op.INSERT, prev);
			}

			if(AdjustableInertialGraph.this.cost[pos*6+ENTER_DELETE] + getCost(Op.DELETE, prev) < cost) {
				op = Op.DELETE;
				cost = AdjustableInertialGraph.this.cost[pos*6+ENTER_DELETE] + getCost(Op.DELETE, prev);
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
