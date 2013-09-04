package org.badiff.q;

import org.badiff.Graph;
import org.badiff.Op;

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
	protected void filter() {
		if(pending.size() >= 2)
			return;
		if(pending.size() == 0 && !shiftPending())
			return;
		if(pending.peekFirst().getOp() != Op.DELETE)
			return;
		if(pending.size() == 1 && !shiftPending())
			return;
		
		Op delete = pending.pollFirst();
		if(pending.peekFirst().getOp() != Op.INSERT) {
			pending.offerFirst(delete);
			return;
		}
		Op insert = pending.pollFirst();
		
		graph.compute(delete.getData(), insert.getData());
		for(Op e : graph.rlist())
			pending.offerFirst(e);
	}
		
	
}
