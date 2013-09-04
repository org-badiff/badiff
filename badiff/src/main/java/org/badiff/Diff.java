package org.badiff;

import java.io.IOException;
import java.util.Iterator;

import org.badiff.q.OpQueue;

public interface Diff extends Applyable {
	public final int DEFAULT_CHUNK = 1024;
	
	public void store(Iterator<Op> ops) throws IOException;
	
	public OpQueue queue() throws IOException;
}
