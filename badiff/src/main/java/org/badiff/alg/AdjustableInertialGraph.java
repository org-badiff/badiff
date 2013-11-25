package org.badiff.alg;

import java.util.ArrayList;
import java.util.List;

import org.badiff.Op;
import org.badiff.q.CompactingOpQueue;
import org.badiff.q.ListOpQueue;
import org.badiff.q.OpQueue;

public class AdjustableInertialGraph implements Graph {
//	1	1	1	1	3	1	3	4	2	2	1	3	1	2	3	1
	
	private static final int[][] DEFAULT_TRANSITION_COSTS = new int[][] {
			{1,	1,	1,	1}, // From Op.STOP to...
			{3,	1,	3,	4}, // From Op.DELETE to...
			{2,	2,	1,	3}, // From Op.INSERT to...
			{1,	2,	3,	1}, // From Op.NEXT to...
//           S  D  I  N
	};
	
	protected static final int DELETE = 0;
	protected static final int INSERT = 1;
	protected static final int NEXT = 2;
	
	protected static final int NUM_FIELDS = 3;
	
	protected float[] cost;
	
	protected final int capacity;
	protected byte[] xval;
	protected byte[] yval;

	protected float stop_stop = DEFAULT_TRANSITION_COSTS[Op.STOP][Op.STOP];

	protected float stop_delete = DEFAULT_TRANSITION_COSTS[Op.STOP][Op.DELETE];

	protected float stop_insert = DEFAULT_TRANSITION_COSTS[Op.STOP][Op.INSERT];

	protected float stop_next = DEFAULT_TRANSITION_COSTS[Op.STOP][Op.NEXT];

	protected float delete_stop = DEFAULT_TRANSITION_COSTS[Op.DELETE][Op.STOP];

	protected float delete_delete = DEFAULT_TRANSITION_COSTS[Op.DELETE][Op.DELETE];

	protected float delete_insert = DEFAULT_TRANSITION_COSTS[Op.DELETE][Op.INSERT];

	protected float delete_next = DEFAULT_TRANSITION_COSTS[Op.DELETE][Op.NEXT];

	protected float insert_stop = DEFAULT_TRANSITION_COSTS[Op.INSERT][Op.STOP];

	protected float insert_delete = DEFAULT_TRANSITION_COSTS[Op.INSERT][Op.DELETE];

	protected float insert_insert = DEFAULT_TRANSITION_COSTS[Op.INSERT][Op.INSERT];

	protected float insert_next = DEFAULT_TRANSITION_COSTS[Op.INSERT][Op.NEXT];

	protected float next_stop = DEFAULT_TRANSITION_COSTS[Op.NEXT][Op.STOP];

	protected float next_delete = DEFAULT_TRANSITION_COSTS[Op.NEXT][Op.DELETE];

	protected float next_insert = DEFAULT_TRANSITION_COSTS[Op.NEXT][Op.INSERT];

	protected float next_next = DEFAULT_TRANSITION_COSTS[Op.NEXT][Op.NEXT];

	/**
	 * Create a new {@link AdjustableInertialGraph} with the given buffer capacity
	 * @param capacity
	 */
	public AdjustableInertialGraph(int capacity) {
		if(capacity < 4)
			throw new IllegalArgumentException("capacity must be >= 4");

		this.capacity = capacity;

		cost = new float[NUM_FIELDS * capacity];
		
	}
	
	public float getCost(byte from, byte to) {
		switch(from) {
		case Op.STOP:
			switch(to) {
			case Op.STOP: return stop_stop;
			case Op.DELETE: return stop_delete;
			case Op.INSERT: return stop_insert;
			case Op.NEXT: return stop_next;
			}
		case Op.DELETE:
			switch(to) {
			case Op.STOP: return delete_stop;
			case Op.DELETE: return delete_delete;
			case Op.INSERT: return delete_insert;
			case Op.NEXT: return delete_next;
			}
		case Op.INSERT:
			switch(to) {
			case Op.STOP: return insert_stop;
			case Op.DELETE: return insert_delete;
			case Op.INSERT: return insert_insert;
			case Op.NEXT: return insert_next;
			}
		case Op.NEXT:
			switch(to) {
			case Op.STOP: return next_stop;
			case Op.DELETE: return next_delete;
			case Op.INSERT: return next_insert;
			case Op.NEXT: return next_next;
			}
		}
		throw new IllegalArgumentException("from:" + from + " to:" + to);
	}

	public void setCost(byte from, byte to, float cost) {
		switch(from) {
		case Op.STOP:
			switch(to) {
			case Op.STOP: stop_stop = cost; return;
			case Op.DELETE: stop_delete = cost; return;
			case Op.INSERT: stop_insert = cost; return;
			case Op.NEXT: stop_next = cost; return;
			}
		case Op.DELETE:
			switch(to) {
			case Op.STOP: delete_stop = cost; return;
			case Op.DELETE: delete_delete = cost; return;
			case Op.INSERT: delete_insert = cost; return;
			case Op.NEXT: delete_next = cost; return;
			}
		case Op.INSERT:
			switch(to) {
			case Op.STOP: insert_stop = cost; return;
			case Op.DELETE: insert_delete = cost; return;
			case Op.INSERT: insert_insert = cost; return;
			case Op.NEXT: insert_next = cost; return;
			}
		case Op.NEXT:
			switch(to) {
			case Op.STOP: next_stop = cost; return;
			case Op.DELETE: next_delete = cost; return;
			case Op.INSERT: next_insert = cost; return;
			case Op.NEXT: next_next = cost; return;
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

		cost[DELETE] = 0;
		cost[INSERT] = 0;
		cost[NEXT] = 0;

		int pos;
		for(int y = 0; y < yval.length; y++) {
			for(int x = 0; x < xval.length; x++) {
				if(x == 0 && y == 0)
					continue;

				pos = y * xval.length + x;
				// mark entry costs
				float edc, eic, enc;

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
				
				float cost;

				// compute delete cost
				cost = edc + delete_delete;
				cost = Math.min(cost, eic + insert_delete);
				cost = Math.min(cost, enc + next_delete);
				this.cost[pos*NUM_FIELDS + DELETE] = (float) Math.min(cost, Short.MAX_VALUE);

				// compute insert cost
				cost = eic + insert_insert; // appending an insert costs 1
				cost = Math.min(cost, edc + delete_insert);
				cost = Math.min(cost, enc + next_insert);
				this.cost[pos*NUM_FIELDS + INSERT] = (float) Math.min(cost, Short.MAX_VALUE);

				// compute next cost
				cost = enc + next_next;
				cost = Math.min(cost, edc + delete_next);
				cost = Math.min(cost, eic + insert_next);
				this.cost[pos*NUM_FIELDS + NEXT] = (float) Math.min(cost, Short.MAX_VALUE);				
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
			x = xval.length - 1;
			y = yval.length - 1;
		}

		@Override
		protected boolean pull() {
			if(pos == 0)
				return false;

			byte op = -1;
			float cost = Float.MAX_VALUE;
			
			if(x > 0 && y > 0 && xval[x] == yval[y]) {
				op = Op.NEXT;
				cost = AdjustableInertialGraph.this.cost[(pos-1-xval.length)*NUM_FIELDS+NEXT] + getCost(Op.NEXT, prev);
			}
			
			if(y > 0 && AdjustableInertialGraph.this.cost[(pos-xval.length)*NUM_FIELDS+INSERT] + getCost(Op.INSERT, prev) < cost) {
				op = Op.INSERT;
				cost = AdjustableInertialGraph.this.cost[(pos-xval.length)*NUM_FIELDS + INSERT] + getCost(Op.INSERT, prev);
			}

			if(x > 0 && AdjustableInertialGraph.this.cost[(pos-1)*NUM_FIELDS+DELETE] + getCost(Op.DELETE, prev) < cost) {
				op = Op.DELETE;
				cost = AdjustableInertialGraph.this.cost[(pos-1)*NUM_FIELDS+DELETE] + getCost(Op.DELETE, prev);
			}

			Op e = null;

			switch(op) {
			case Op.NEXT:
				e = new Op(Op.NEXT, 1, null);
				pos = pos - 1 - xval.length;
				x--; y--;
				break;
			case Op.INSERT:
				e = new Op(Op.INSERT, 1, new byte[] {yval[pos / xval.length]});
				pos = pos - xval.length;
				y--;
				break;
			case Op.DELETE:
				e = new Op(Op.DELETE, 1, new byte[] {xval[pos % xval.length]});
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
