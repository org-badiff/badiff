package org.badiff.imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * make it easy to work with.
 * @author robin
 *
 */
public class FileDiff extends File implements Diff, Serialized {
	private static final long serialVersionUID = 0;
	
	protected Serialization serial = DefaultSerialization.getInstance();

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
	
	public FileDiff(File parent, String child, Serialization serialization) {
		super(parent, child);
		this.serial = serialization;
	}

	public FileDiff(String parent, String child, Serialization serialization) {
		super(parent, child);
		this.serial = serialization;
	}

	public FileDiff(String pathname, Serialization serialization) {
		super(pathname);
		this.serial = serialization;
	}

	public FileDiff(URI uri, Serialization serialization) {
		super(uri);
		this.serial = serialization;
	}

	public FileDiff(File file, Serialization serialization) {
		this(file.toURI());
		this.serial = serialization;
	}
	
	@Override
	public void apply(InputStream orig, OutputStream target) throws IOException {
		InputStream self = new FileInputStream(this);
		try {
			long count = serial.readObject(self, Long.class);
			for(long i = 0; i < count; i++)
				serial.readObject(self, Op.class).apply(orig, target);
		} finally { 
			self.close();
		}
	}
	
	@Override
	public OpQueue queue() throws IOException {
		return new FileOpQueue();
	}
	
	@Override
	public void store(Iterator<Op> ops) throws IOException {
		FileOutputStream out = new FileOutputStream(this);
		try {
			while(ops.hasNext()) {
				serial.writeObject(out, Op.class, ops.next());
			}
			serial.writeObject(out, Op.class, new Op(Op.STOP, 1, null));
		} finally {
			out.close();
		}
	}
	
	private class FileOpQueue extends OpQueue {
		private InputStream self;
		private boolean closed;
		
		public FileOpQueue() throws IOException {
			self = new FileInputStream(FileDiff.this);
			closed = false;
		}
		
		@Override
		public boolean offer(Op e) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected void shift() {
			if(!closed) {
				try {
					Op e = serial.readObject(self, Op.class);
					if(e.getOp() != Op.STOP)
						super.offer(e);
					else
						close();
				} catch(IOException ioe) {
					close();
					throw new RuntimeIOException(ioe);
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
	}

	@Override
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		/*
		 * Since the raw file contents are already serialized, just dump
		 * the file to the output stream.
		 */
		InputStream in = new FileInputStream(this);
		try {
			Streams.copy(in, out);
		} finally {
			in.close();
		}
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		/*
		 * The input stream has to be processed to determine when to stop reading,
		 * unlike serialize(...) which can just dump the file to the output stream
		 */
		FileOutputStream out = new FileOutputStream(this);
		try {
			for(Op e = serial.readObject(in, Op.class); e.getOp() != Op.STOP; e = serial.readObject(in, Op.class))
				this.serial.writeObject(out, Op.class, e);
			this.serial.writeObject(out, Op.class, new Op(Op.STOP, 1, null));
		} finally {
			out.close();
		}
	}
}
