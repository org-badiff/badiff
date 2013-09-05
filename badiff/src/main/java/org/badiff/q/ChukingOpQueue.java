package org.badiff.q;

import org.badiff.Diff;
import org.badiff.DiffOp;

public class ChukingOpQueue extends FilterOpQueue {

	protected int chunk;
	
	public ChukingOpQueue(OpQueue source) {
		this(source, Diff.DEFAULT_CHUNK);
	}
	
	public ChukingOpQueue(OpQueue source, int chunk) {
		super(source);
		this.chunk = chunk;
	}

	@Override
	protected void filter() {
		if(pending.size() >= 2)
			return;
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
		
		byte[] ddata = delete.getData();
		byte[] idata = insert.getData();
		
		int dpos = 0;
		int ipos = 0;
		
		while(dpos < ddata.length && ipos < idata.length) {
			if(dpos < ddata.length) {
				byte[] data = new byte[Math.min(chunk, ddata.length - dpos)];
				System.arraycopy(ddata, dpos, data, 0, data.length);
				pending.offerLast(new DiffOp(DiffOp.DELETE, data.length, data));
				dpos += data.length;
			}
			if(ipos < idata.length) {
				byte[] data = new byte[Math.min(chunk, idata.length - ipos)];
				System.arraycopy(idata, ipos, data, 0, data.length);
				pending.offerLast(new DiffOp(DiffOp.INSERT, data.length, data));
				ipos += data.length;
			}
		}
	}

}
