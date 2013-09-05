package org.badiff.q;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.RuntimeIOException;

/**
 * {@link OpQueue} that lazily reads from two input streams (original and target)
 * and produces chunked output similar to {@link ChunkingOpQueue}.
 * @author robin
 *
 */
public class StreamChunkingOpQueue extends OpQueue {
	protected InputStream orig;
	protected InputStream target;
	protected int chunk;
	
	/**
	 * Create a {@link StreamChunkingOpQueue} with the default chunk size
	 * @param orig
	 * @param target
	 */
	public StreamChunkingOpQueue(InputStream orig, InputStream target) {
		this(orig, target, Diff.DEFAULT_CHUNK);
	}
	
	/**
	 * Create an {@link OpQueue} lazily populated with alternating chunks of data read
	 * from the streams.
	 * @param orig The source of {@link Op#DELETE} chunks
	 * @param target The source of {@link Op#INSERT} chunks
	 * @param chunk
	 */
	public StreamChunkingOpQueue(InputStream orig, InputStream target, int chunk) {
		this.orig = orig;
		this.target = target;
		this.chunk = chunk;
	}
	
	@Override
	protected void shift() {
		if(pending.size() == 0) {
			/*
			 * Lazily offer new chunks if available
			 */
			byte[] obuf = readChunk(orig);
			byte[] tbuf = readChunk(target);
			
			if(obuf != null)
				pending.offerLast(new Op(Op.DELETE, obuf.length, obuf));
			if(tbuf != null)
				pending.offerLast(new Op(Op.INSERT, tbuf.length, tbuf));
		}
		super.shift();
	}

	/**
	 * Read a chunk from the {@link InputStream}
	 * @param in
	 * @return
	 */
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
