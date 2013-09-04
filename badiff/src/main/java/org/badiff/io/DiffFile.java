package org.badiff.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.badiff.BADiff;
import org.badiff.Op;
import org.badiff.q.OpQueue;
import org.badiff.util.Streams;

public abstract class DiffFile extends File implements BADiff {
	
	protected abstract Serialization serialization();

	public DiffFile(File parent, String child) {
		super(parent, child);
	}

	public DiffFile(String parent, String child) {
		super(parent, child);
	}

	public DiffFile(String pathname) {
		super(pathname);
	}

	public DiffFile(URI uri) {
		super(uri);
	}

	public DiffFile(File file) {
		this(file.toURI());
	}
	
	@Override
	public void applyDiff(InputStream orig, OutputStream target)
			throws IOException {
		InputStream self = new FileInputStream(this);
		try {
			long count = serialization().readObject(self, Long.class);
			for(long i = 0; i < count; i++)
				serialization().readObject(self, Op.class).applyOp(orig, target);
		} finally { 
			self.close();
		}
	}
	
	public OpQueue queue() throws IOException {
		return new FileOpQueue();
	}
	
	public long write(OpQueue q) throws IOException {
		long count = 0;
		File tmp = File.createTempFile(getName(), ".tmp");
		
		FileOutputStream out = new FileOutputStream(tmp);
		for(Op e = q.poll(); e != null; e = q.poll()) {
			serialization().writeObject(out, e);
			count++;
		}
		out.close();
		
		out = new FileOutputStream(this);
		InputStream in = new FileInputStream(tmp);
		serialization().writeObject(out, count);
		Streams.copy(in, out);
		in.close();
		out.close();
		
		tmp.delete();
		return count;
	}

	private class FileOpQueue extends OpQueue {
		private InputStream self;
		private long count;
		private long i;
		private boolean closed;
		
		public FileOpQueue() throws IOException {
			self = new FileInputStream(DiffFile.this);
			count = serialization().readObject(self, Long.class);
			i = 0;
			closed = false;
			if(count == 0)
				close();
		}
		
		@Override
		protected void shift() {
			if(!closed && i < count) {
				try {
					pending.offerLast(serialization().readObject(self, Op.class));
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
	}
}
