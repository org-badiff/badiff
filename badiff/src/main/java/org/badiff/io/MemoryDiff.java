package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.badiff.Diff;
import org.badiff.DiffOp;
import org.badiff.q.ListOpQueue;
import org.badiff.q.OpQueue;

public class MemoryDiff implements Diff {
	protected List<DiffOp> ops = new ArrayList<DiffOp>();

	@Override
	public void apply(InputStream orig, OutputStream target)
			throws IOException {
		for(DiffOp e : ops)
			e.apply(orig, target);
	}

	@Override
	public void store(Iterator<DiffOp> ops) {
		this.ops.clear();
		while(ops.hasNext())
			this.ops.add(ops.next());
	}

	@Override
	public OpQueue queue() {
		return new MemoryOpQueue(ops);
	}

	private class MemoryOpQueue extends ListOpQueue {
		private MemoryOpQueue(List<DiffOp> ops) {
			super(ops);
		}
	
		@Override
		public boolean offer(DiffOp e) {
			throw new UnsupportedOperationException();
		}
	}

}
