package org.badiff.imp;

import java.io.IOException;
import java.io.InputStream;

import org.badiff.Op;
import org.badiff.Queueable;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialization;
import org.badiff.q.OpQueue;

public class StreamQueueable implements Queueable {
	
	protected Serialization serial;
	protected InputStream in;

	public StreamQueueable(InputStream in) {
		this(in, DefaultSerialization.getInstance());
	}
	
	public StreamQueueable(InputStream in, Serialization serial) {
		this.in = in;
		this.serial = serial;
	}
	
	@Override
	public OpQueue queue() throws IOException {
		return new StreamOpQueue();
	}
	
	private class StreamOpQueue extends OpQueue {
		private boolean closed;
		
		@Override
		public boolean offer(Op e) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected void shift() {
			if(closed) {
				super.shift();
				return;
			}
			try {
				Op e = serial.readObject(in, Op.class);
				if(e.getOp() == Op.STOP)
					closed = true;
				else
					super.offer(e);
			} catch(IOException ioe) {
				throw new RuntimeIOException(ioe);
			}
			super.shift();
		}
	}

}
