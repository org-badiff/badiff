package org.badiff.p;

import org.badiff.q.OpQueue;

public interface Pipe {
	public Pipeline from(OpQueue q);
}
