package org.badiff.imp;

import java.io.IOException;
import java.io.InputStream;

import org.badiff.Op;
import org.badiff.Queueable;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialization;
import org.badiff.q.OpQueue;

/**
 * {@link Queueable} that reads its elements from an {@link InputStream}.
 * The {@link #queue()} method can be called only once; calling it again
 * will return the <b>next</b> sequence of {@link Op}s in the input stream.
 * @author robin
 *
 */
public class StreamQueueable implements Queueable {
	/**
	 * Serialization used for this stream
	 */
	protected Serialization serial;
	/**
	 * The input stream
	 */
	protected InputStream in;

	/**
	 * Create a {@link StreamQueueable} with the {@link DefaultSerialization}
	 * @param in
	 */
	public StreamQueueable(InputStream in) {
		this(in, DefaultSerialization.getInstance());
	}
	
	/**
	 * Create a {@link StreamQueueable}
	 * @param in
	 * @param serial
	 */
	public StreamQueueable(InputStream in, Serialization serial) {
		this.in = in;
		this.serial = serial;
	}
	
	@Override
	public OpQueue queue() throws IOException {
		return new StreamOpQueue();
	}
	
	/**
	 * {@link OpQueue} that reads from a stream
	 * @author robin
	 *
	 */
	private class StreamOpQueue extends OpQueue {
		private boolean closed;
		
		@Override
		public boolean offer(Op e) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected boolean pull() {
			if(closed)
				return false;
			
			try {
				Op e = serial.readObject(in, Op.class);
				if(e.getOp() == Op.STOP) {
					closed = true;
					return false;
				} else {
					prepare(e);
					return true;
				}
			} catch(IOException ioe) {
				throw new RuntimeIOException(ioe);
			}
		}
	}

}
