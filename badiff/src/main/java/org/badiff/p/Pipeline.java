package org.badiff.p;

import org.badiff.q.OpQueue;

public class Pipeline {

	protected OpQueue q;
	
	public Pipeline(OpQueue q) {
		this.q = q;
		
	}
	
	public Pipeline into(Pipe... pipes) {
		Pipeline pl = this;
		for(Pipe p : pipes)
			pl = p.from(pl.drain());
		return pl;
	}

	public OpQueue drain() {
		return q;
	}

}
