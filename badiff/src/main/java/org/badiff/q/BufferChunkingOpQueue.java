package org.badiff.q;

import java.nio.ByteBuffer;

import org.badiff.Diff;
import org.badiff.Op;

public class BufferChunkingOpQueue extends OpQueue {
	
	protected ByteBuffer orig;
	protected ByteBuffer target;
	protected int chunk;
	
	public BufferChunkingOpQueue(ByteBuffer orig, ByteBuffer target) {
		this(orig, target, Diff.DEFAULT_CHUNK);
	}
	
	public BufferChunkingOpQueue(ByteBuffer orig, ByteBuffer target, int chunk) {
		this.orig = orig;
		this.target = target;
		this.chunk = chunk;
	}
	
	@Override
	protected void shift() {
		if(pending.size() == 0) {
			byte[] obuf = readChunk(orig);
			byte[] tbuf = readChunk(target);
			
			if(obuf != null)
				pending.offerLast(new Op(Op.DELETE, obuf.length, obuf));
			if(tbuf != null)
				pending.offerLast(new Op(Op.INSERT, tbuf.length, tbuf));
		}
		super.shift();
	}
	
	protected byte[] readChunk(ByteBuffer in) {
		if(in.remaining() == 0)
			return null;
		byte[] buf = new byte[Math.min(chunk, in.remaining())];
		in.get(buf);
		return buf;
	}
}
