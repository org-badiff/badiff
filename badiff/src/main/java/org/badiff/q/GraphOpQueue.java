package org.badiff.q;

import java.util.Collections;
import java.util.List;

import org.badiff.Op;
import org.badiff.alg.Graph;

/**
 * {@link OpQueue} that replaces ({@link Op#DELETE},{@link Op#INSERT}) pairs
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
		List<Op> rlist = graph.rlist();
		Collections.reverse(rlist);
		for(Op e : rlist)
			ready.offerLast(e);
	}
		
	
}
