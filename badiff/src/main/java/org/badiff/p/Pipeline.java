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
			pl = p.from(pl.outlet());
		return pl;
	}
	
	public Pipeline into(String codes) {
		return into(Pipes.fromCodes(codes));
	}

	public OpQueue outlet() {
		return q;
	}

}
