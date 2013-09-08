/**
 * badiff - byte array diff - fast pure-java byte-level diffing
 * 
 * Copyright (c) 2013, Robin Kirkman All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3) Neither the name of the badiff nor the names of its contributors may be 
 *    used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import org.badiff.io.Input;
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
	public void apply(Input orig, OutputStream target) throws IOException {
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
