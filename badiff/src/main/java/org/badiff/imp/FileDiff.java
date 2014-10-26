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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.q.OpQueue;
import org.badiff.util.Data;

/**
 * {@link Diff} that is backed by a {@link File}.  Extends {@link File} to
 * make it easy to work with.
 * @author robin
 *
 */
public class FileDiff extends File implements Diff, Serialized {
	private static final long serialVersionUID = 0;
	
	protected Serialization serial = DefaultSerialization.newInstance();

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
	public void apply(DataInput orig, DataOutput target) throws IOException {
		OpQueue q = queue();
		for(Op e = q.poll(); e != null; e = q.poll())
			e.apply(orig, target);
	}
	
	@Override
	public OpQueue queue() throws IOException {
		return new FileOpQueue();
	}
	
	@Override
	public void store(Iterator<Op> ops) throws IOException {
		FileOutputStream out = new FileOutputStream(this);
		DataOutput data = new DataOutputStream(out);
		try {
			while(ops.hasNext()) {
				serial.writeObject(data, Op.class, ops.next());
			}
			serial.writeObject(data, Op.class, new Op(Op.STOP, 1, null));
		} finally {
			out.close();
		}
	}
	
	private class FileOpQueue extends OpQueue {
		private InputStream self;
		private DataInput data;
		private boolean closed;
		
		public FileOpQueue() throws IOException {
			self = new FileInputStream(FileDiff.this);
			data = new DataInputStream(self);
			closed = false;
		}
		
		@Override
		public boolean offer(Op e) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected boolean pull() {
			if(!closed) {
				try {
					Op e = serial.readObject(data, Op.class);
					if(e.getOp() != Op.STOP) {
						prepare(e);
						return true;
					} else
						close();
				} catch(IOException ioe) {
					close();
					throw new RuntimeIOException(ioe);
				}
			}
			return false;
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
		public String toString() {
			return getName();
		}
	}

	@Override
	public void serialize(Serialization serial, DataOutput out)
			throws IOException {
		/*
		 * Since the raw file contents are already serialized, just dump
		 * the file to the output stream.
		 */
		InputStream in = new FileInputStream(this);
		try {
			Data.copy(new DataInputStream(in), out);
		} finally {
			in.close();
		}
	}

	@Override
	public void deserialize(Serialization serial, DataInput in)
			throws IOException {
		/*
		 * The input stream has to be processed to determine when to stop reading,
		 * unlike serialize(...) which can just dump the file to the output stream
		 */
		FileOutputStream out = new FileOutputStream(this);
		DataOutput data = new DataOutputStream(out);
		try {
			for(Op e = serial.readObject(in, Op.class); e.getOp() != Op.STOP; e = serial.readObject(in, Op.class))
				this.serial.writeObject(data, Op.class, e);
			this.serial.writeObject(data, Op.class, new Op(Op.STOP, 1, null));
		} finally {
			out.close();
		}
	}
}
