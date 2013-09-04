package org.badiff.q;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.RuntimeIOException;

public class StreamChunkingOpQueue extends OpQueue {
	protected InputStream orig;
	protected InputStream target;
	protected int chunk;
	
	public StreamChunkingOpQueue(InputStream orig, InputStream target) {
		this(orig, target, Diff.DEFAULT_CHUNK);
	}
	
	public StreamChunkingOpQueue(InputStream orig, InputStream target, int chunk) {
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

	protected byte[] readChunk(InputStream in) {
		try {
			byte[] buf = new byte[chunk];
			int r = in.read(buf);
			if(r == -1)
				return null;
			if(r == chunk)
				return buf;
			return Arrays.copyOf(buf, r);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}
	
}
