package org.badiff.q;

import org.badiff.DiffOp;
import org.badiff.alg.Graph;

/**
 * {@link OpQueue} that replaces ({@link DiffOp#DELETE},{@link DiffOp#INSERT}) pairs
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
	protected void filter() {
		if(pending.size() == 0 && !shiftPending())
			return;
		if(pending.peekFirst().getOp() != DiffOp.DELETE)
			return;
		if(pending.size() == 1 && !shiftPending())
			return;
		
		DiffOp delete = pending.pollFirst();
		if(pending.peekFirst().getOp() != DiffOp.INSERT) {
			pending.offerFirst(delete);
			return;
		}
		DiffOp insert = pending.pollFirst();
		
		graph.compute(delete.getData(), insert.getData());
		for(DiffOp e : graph.rlist())
			pending.offerFirst(e);
	}
		
	
}
