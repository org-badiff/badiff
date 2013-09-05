package org.badiff.imp;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.q.OpQueue;
import org.badiff.util.Streams;

/**
 * {@link Diff} that is backed by a {@link File}.  Extends {@link File} to
 * make it easy to work with.  This class is abstract so that subclasses can
 * provide their own {@link Serialization} mechanism.
 * @author robin
 *
 */
public abstract class FileDiff extends File implements Diff {
	private static final long serialVersionUID = 0;
	
	/**
	 * Returns the {@link Serialization} mechanism for this {@link FileDiff}
	 * @return
	 */
	protected abstract Serialization serialization();

	public FileDiff(File parent, String child) {
		super(parent, child);
	}

	public FileDiff(String parent, String child) {
		super(parent, child);
	}

	public FileDiff(String pathname) {
		super(pathname);
	}

	public FileDiff(URI uri) {
		super(uri);
	}

	public FileDiff(File file) {
		this(file.toURI());
	}
	
	@Override
	public void apply(InputStream orig, OutputStream target) throws IOException {
		InputStream self = new FileInputStream(this);
		try {
			long count = serialization().readObject(self, Long.class);
			for(long i = 0; i < count; i++)
				serialization().readObject(self, Op.class).apply(orig, target);
		} finally { 
			self.close();
		}
	}
	
	@Override
	public OpQueue queue() throws IOException {
		return new FileOpQueue(this);
	}
	
	@Override
	public void store(Iterator<Op> ops) throws IOException {
		write(ops);
	}
	
	/**
	 * Write the argument operations to this {@link FileDiff}
	 * @param q
	 * @return The number of operations written
	 * @throws IOException
	 */
	public long write(Iterator<Op> q) throws IOException {
		long count = 0;
		File tmp = File.createTempFile(getName(), ".tmp");
		
		FileOutputStream out = new FileOutputStream(tmp);
		while(q.hasNext()) {
			Op e = q.next();
			serialization().writeObject(out, Op.class, e);
			count++;
		}
		out.close();
		
		out = new FileOutputStream(this);
		InputStream in = new FileInputStream(tmp);
		serialization().writeObject(out, Long.class, count);
		Streams.copy(in, out);
		in.close();
		out.close();
		
		tmp.delete();
		return count;
	}
	
	private static class FileOpQueue extends OpQueue implements Serialized, Externalizable {
		private FileDiff thiz;
		private InputStream self;
		private long count;
		private long i;
		private boolean closed;
		
		/*
		 * Required for deserialization
		 */
		@SuppressWarnings("unused")
		public FileOpQueue() {
		}
		
		public FileOpQueue(FileDiff thiz) throws IOException {
			this.thiz = thiz;
			self = new FileInputStream(thiz);
			count = thiz.serialization().readObject(self, Long.class);
			i = 0;
			closed = false;
			if(count == 0)
				close();
		}
		
		@Override
		public boolean offer(Op e) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected void shift() {
			if(!closed && i < count) {
				try {
					super.offer(thiz.serialization().readObject(self, Op.class));
					i++;
				} catch(IOException ioe) {
					close();
					throw new RuntimeIOException(ioe);
				} finally {
					if(i == count)
						close();
				}
			}
			super.shift();
		}
		
		private void close() {
			try {
				self.close();
			} catch(IOException ioe) {
				throw new RuntimeIOException(ioe);
			} finally {
				closed = true;
			}
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			serialize(DefaultSerialization.getInstance(), Streams.asStream(out));
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException,
				ClassNotFoundException {
			deserialize(DefaultSerialization.getInstance(), Streams.asStream(in));
		}

		@Override
		public void serialize(Serialization serial, OutputStream out)
				throws IOException {
			serial.writeObject(out, Class.class, thiz.getClass());
			serial.writeObject(out, Long.class, i);
			serial.writeObject(out, Long.class, thiz.length());
			InputStream in = new FileInputStream(thiz);
			Streams.copy(in, out);
			in.close();
		}

		@Override
		public void deserialize(Serialization serial, InputStream in)
				throws IOException {
			@SuppressWarnings("unchecked")
			Class<? extends FileDiff> type = serial.readObject(in, Class.class);
			try {
				thiz = type.getConstructor(File.class).newInstance(File.createTempFile("FileOpQueue", ".tmp"));
			} catch(Exception ex) {
				throw new IOException("Unable to instantiate " + type, ex);
			}
			thiz.deleteOnExit();
			
			long i = serial.readObject(in, Long.class);
			
			long length = serial.readObject(in, Long.class);
			OutputStream out = new FileOutputStream(thiz);
			Streams.copy(in, out, length);
			out.close();
			
			self = new FileInputStream(thiz);
			count = thiz.serialization().readObject(self, Long.class);
			if(count == 0)
				close();
			while(this.i < i)
				poll();
		}
	}
}
