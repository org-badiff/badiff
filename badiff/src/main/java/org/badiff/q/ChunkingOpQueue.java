package org.badiff.q;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.alg.Graph;

/**
 * {@link OpQueue} that lazily chunks pairs of pending
 * DiffOp's with the types ({@link Op#DELETE},{@link Op#INSERT}).
 * Chunking means removing the two pending operations and replacing them
 * with alternating {@link Op#DELETE} and {@link Op#INSERT} operations
 * whose {@link Op#getRun()} length is no greater than the chunk size.<p>
 * 
 * Chunking is used primarily to pre-process input to other algorithms,
 * such as {@link Graph}, into manageable sizes.
 * @author robin
 *
 */
public class ChunkingOpQueue extends FilterOpQueue {

	/**
	 * The chunk size
	 */
	protected int chunk;
	
	/**
	 * Create a {@link ChunkingOpQueue} with a default chunk size
	 * @param source
	 */
	public ChunkingOpQueue(OpQueue source) {
		this(source, Diff.DEFAULT_CHUNK);
	}
	
	/**
	 * Create a {@link ChunkingOpQueue} with a specified chunk size
	 * @param source
	 * @param chunk
	 */
	public ChunkingOpQueue(OpQueue source, int chunk) {
		super(source);
		this.chunk = chunk;
	}

	@Override
	protected void filter() {
		/*
		 * Look for a (DELETE,INSERT) pair at the head of the pending queue
		 */
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
		
		/*
		 * Chunk the delete and insert
		 */
		
		byte[] ddata = delete.getData();
		byte[] idata = insert.getData();
		
		int dpos = 0;
		int ipos = 0;
		
		while(dpos < ddata.length || ipos < idata.length) {
			if(dpos < ddata.length) {
				byte[] data = new byte[Math.min(chunk, ddata.length - dpos)];
				System.arraycopy(ddata, dpos, data, 0, data.length);
				ready.offerLast(new Op(Op.DELETE, data.length, data));
				dpos += data.length;
			}
			if(ipos < idata.length) {
				byte[] data = new byte[Math.min(chunk, idata.length - ipos)];
				System.arraycopy(idata, ipos, data, 0, data.length);
				ready.offerLast(new Op(Op.INSERT, data.length, data));
				ipos += data.length;
			}
		}
	}

}
