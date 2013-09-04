package org.badiff.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.badiff.BADiff;
import org.badiff.Op;
import org.badiff.q.OpQueue;

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
