package org.badiff.q;

import java.util.List;
import java.util.ListIterator;

import org.badiff.Op;
import org.badiff.alg.InnertialGraph;

public class InertialGraphOpQueue extends FilterOpQueue {
	
	protected InnertialGraph graph;
	
	public InertialGraphOpQueue(OpQueue source, int chunk) {
		this(source, new InnertialGraph((chunk + 1) * (chunk + 1)));
	}
	
	public InertialGraphOpQueue(OpQueue source, InnertialGraph graph) {
		super(source);
		this.graph = graph;
	}

	@Override
	protected boolean pull() {
		if(!require(2))
			return flush();
		
		Op delete = null;
		Op insert = null;
		
		if(filtering.get(0).getOp() == Op.DELETE && filtering.get(1).getOp() == Op.INSERT) {
			delete = filtering.get(0);
			insert = filtering.get(1);
		} else if(filtering.get(0).getOp() == Op.INSERT && filtering.get(1).getOp() == Op.DELETE) {
			delete = filtering.get(1);
			insert = filtering.get(0);
		} else
			return flush();
		
		filtering.remove(1);
		filtering.remove(0);
		
		graph.compute(delete.getData(), insert.getData());
		OpQueue q = graph.queue();
		for(Op e = q.poll(); e != null; e = q.poll())
			prepare(e);
		
		return true;
	}

}
