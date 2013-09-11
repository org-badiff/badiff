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

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.DataOutputOutputStream;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.Random;
import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.io.SmallNumberSerialization;
import org.badiff.q.OpQueue;
import org.badiff.util.Streams;

public class BadiffFileDiff extends File implements Diff, Serialized {
	private static final long serialVersionUID = 0;

	public static final byte[] MAGIC = new byte[] {0, (byte)0xde, (byte)0xee, (byte)0xef};
	public static final int VERSION = 1;

	public static final long FLAG_RANDOM_ACCESS = 0x1;
	public static final long FLAG_DEFAULT_SERIALIZATION = 0x2;
	public static final long FLAG_SMALL_NUMBER_SERIALIZATION = 0x4;
	public static final long FLAG_UNSPECIFIED_SERIALIZATION = 0x8;
	public static final long FLAG_OPTIONAL_DATA = 0x10;
	
	public static class Stats implements Serialized {
		private long rewindCount;
		private long nextCount;
		private long insertCount;
		private long deleteCount;
		
		private Stats() {}
		
		@Override
		public void serialize(Serialization serial, OutputStream out)
				throws IOException {
			serial.writeObject(out, Long.class, rewindCount);
			serial.writeObject(out, Long.class, nextCount);
			serial.writeObject(out, Long.class, insertCount);
			serial.writeObject(out, Long.class, deleteCount);
		}
		@Override
		public void deserialize(Serialization serial, InputStream in)
				throws IOException {
			rewindCount = serial.readObject(in, Long.class);
			nextCount = serial.readObject(in, Long.class);
			insertCount = serial.readObject(in, Long.class);
			deleteCount = serial.readObject(in, Long.class);
		}

		public long getRewindCount() {
			return rewindCount;
		}

		public long getNextCount() {
			return nextCount;
		}

		public long getInsertCount() {
			return insertCount;
		}

		public long getDeleteCount() {
			return deleteCount;
		}
	}
	
	public static class Header {
		private byte[] magic;
		private int version;
		private long flags;
		private Serialization serial;
		private Stats stats;
		private Optional optional;
		
		private Header() {}

		public byte[] getMagic() {
			return magic;
		}

		public int getVersion() {
			return version;
		}

		public long getFlags() {
			return flags;
		}

		public Serialization getSerial() {
			return serial;
		}

		public Stats getStats() {
			return stats;
		}
		
		public Optional getOptional() {
			return optional;
		}
	}
	
	public static class Optional implements Serialized {
		private byte[] preHash;
		private byte[] postHash;
		private String hashAlgorithm;
		
		public byte[] getPreHash() {
			return preHash;
		}
		public void setPreHash(byte[] preHash) {
			this.preHash = preHash;
		}
		public byte[] getPostHash() {
			return postHash;
		}
		public void setPostHash(byte[] postHash) {
			this.postHash = postHash;
		}
		@Override
		public void serialize(Serialization serial, OutputStream out)
				throws IOException {
			serial.writeObject(out, String.class, hashAlgorithm);
			serial.writeObject(out, byte[].class, preHash);
			serial.writeObject(out, byte[].class, postHash);
		}
		@Override
		public void deserialize(Serialization serial, InputStream in)
				throws IOException {
			hashAlgorithm = serial.readObject(in, String.class);
			preHash = serial.readObject(in, byte[].class);
			postHash = serial.readObject(in, byte[].class);
		}
		public String getHashAlgorithm() {
			return hashAlgorithm;
		}
		public void setHashAlgorithm(String hashAlgorithm) {
			this.hashAlgorithm = hashAlgorithm;
		}
	}
	
	protected static Stats computeStats(Diff diff) throws IOException {
		Stats stats = new Stats();
		OpQueue q = diff.queue();
		for(Op e = q.poll(); e != null; e = q.poll()) {
			switch(e.getOp()) {
			case Op.DELETE:
				stats.deleteCount++;
				if(e.getRun() < 0)
					stats.rewindCount++;
				break;
				
			case Op.INSERT:
				stats.insertCount++;
				break;
				
			case Op.NEXT:
				stats.nextCount++;
				break;
			}
		}
		return stats;
	}
	
	protected Serialization serial = null;
	
	public BadiffFileDiff(String pathname) {
		super(pathname);
	}

	public BadiffFileDiff(URI uri) {
		super(uri);
	}

	public BadiffFileDiff(String parent, String child) {
		super(parent, child);
	}

	public BadiffFileDiff(File parent, String child) {
		super(parent, child);
	}

	public BadiffFileDiff(File file) {
		super(file.toURI());
	}
	
	public BadiffFileDiff(String pathname, Serialization serial) {
		super(pathname);
		this.serial = serial;
	}

	public BadiffFileDiff(URI uri, Serialization serial) {
		super(uri);
		this.serial = serial;
	}

	public BadiffFileDiff(String parent, String child, Serialization serial) {
		super(parent, child);
		this.serial = serial;
	}

	public BadiffFileDiff(File parent, String child, Serialization serial) {
		super(parent, child);
		this.serial = serial;
	}

	public BadiffFileDiff(File file, Serialization serial) {
		super(file.toURI());
		this.serial = serial;
	}

	protected static void writeHeader(Serialization serial, Stats stats, Optional opt, DataOutput out) throws IOException {
		long flags = 0;
		
		if(stats.rewindCount > 0)
			flags |= FLAG_RANDOM_ACCESS;
		
		if(serial == DefaultSerialization.getInstance())
			flags |= FLAG_DEFAULT_SERIALIZATION;
		else if(serial == SmallNumberSerialization.getInstance())
			flags |= FLAG_SMALL_NUMBER_SERIALIZATION;
		else
			flags |= FLAG_UNSPECIFIED_SERIALIZATION;
		
		if(opt != null)
			flags |= FLAG_OPTIONAL_DATA;
		
		out.write(MAGIC);
		out.writeInt(VERSION);
		out.writeLong(flags);
		
		DataOutputOutputStream dout = new DataOutputOutputStream(out);
		
		stats.serialize(serial, dout);
		
		if(opt != null)
			opt.serialize(serial, dout);
	}
	
	protected Header readHeader(DataInputStream in) throws IOException {
		byte[] magic = new byte[MAGIC.length];
		in.read(magic);
		if(!Arrays.equals(magic, MAGIC))
			throw new IOException("Invalid badiff magic");
		
		int version = in.readInt();
		if(version < 1 || version > VERSION)
			throw new IOException("Unrecognized version");
		
		long flags = in.readLong();
		
		Serialization serial = this.serial;
		
		if((flags & FLAG_DEFAULT_SERIALIZATION) != 0) {
			if(serial != null && serial != DefaultSerialization.getInstance())
				throw new IOException(
						"Incompatible serialization; expected " 
								+ serial.getClass().getSimpleName() + ", file declares " 
								+ DefaultSerialization.getInstance().getClass().getSimpleName());
			else if(serial == null)
				serial = DefaultSerialization.getInstance();
		}
		if((flags & FLAG_SMALL_NUMBER_SERIALIZATION) != 0) {
			if(serial != null && serial != SmallNumberSerialization.getInstance())
				throw new IOException(
						"Incompatible serialization; expected " 
								+ serial.getClass().getSimpleName() + ", file declares " 
								+ SmallNumberSerialization.getInstance().getClass().getSimpleName());
			else if(serial == null)
				serial = SmallNumberSerialization.getInstance();
		}
		if((flags & FLAG_UNSPECIFIED_SERIALIZATION) != 0) {
			if(serial == null)
				throw new IOException("Incompatible serialization; expected file to specify, file declares unspecified");
		}
		
		Stats stats = new Stats();
		stats.deserialize(serial, in);
		
		Optional opt = null;
		if((flags & FLAG_OPTIONAL_DATA) != 0) {
			opt = new Optional();
			opt.deserialize(serial, in);
		}
		
		Header header = new Header();
		header.magic = magic;
		header.version = version;
		header.flags = flags;
		header.serial = serial;
		header.stats = stats;
		header.optional = opt;
		
		return header;
	}

	public Header header() throws IOException {
		DataInputStream in = new DataInputStream(new FileInputStream(this));
		Header header = readHeader(in);
		in.close();
		return header;
	}
	
	public Stats stats() throws IOException {
		return header().stats;
	}
	
	@Override
	public void apply(InputStream orig, OutputStream target) throws IOException {
		Header header = header();
		if((header.flags & FLAG_RANDOM_ACCESS) != 0 && !(orig instanceof Random))
			throw new IOException(this + " requires a random-access input (" + Random.class + ")");
		OpQueue q = queue();
		for(Op e = q.poll(); e != null; e = q.poll())
			e.apply(orig, target);
	}

	@Override
	public void store(Iterator<Op> ops) throws IOException {
		DataOutputStream out = new DataOutputStream(new FileOutputStream(this));
		store(out, serial, null, ops);
		out.close();
	}
	
	public static void store(DataOutput out, Serialization serial, Optional opt, Iterator<Op> ops) throws IOException {
		/* 
		 * shove the ops into a temp FileDiff first so we can compute some stats
		 * without having them all in memory
		 */
		FileDiff tmp = new FileDiff(File.createTempFile("filediff", ".tmp"));
		tmp.store(ops);
		
		// Compute the stats
		Stats stats = computeStats(tmp);
		
		if(serial == null)
			serial = DefaultSerialization.getInstance();
		
		// Write the header
		writeHeader(serial, stats, opt, out);
		
		DataOutputOutputStream dout = new DataOutputOutputStream(out);
		
		// Copy the ops
		OpQueue q = tmp.queue();
		for(Op e = q.poll(); e != null; e = q.poll())
			serial.writeObject(dout, Op.class, e);
		serial.writeObject(dout, Op.class, new Op(Op.STOP, 1, null));
		
		tmp.delete();
	}

	@Override
	public OpQueue queue() throws IOException {
		return new FileBadiffOpQueue();
	}
	
	private class FileBadiffOpQueue extends OpQueue {
		private Header header;
		private DataInputStream self;
		private boolean closed;
		
		public FileBadiffOpQueue() throws IOException {
			self = new DataInputStream(new FileInputStream(BadiffFileDiff.this));
			header = readHeader(self);
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
					Op e = header.serial.readObject(self, Op.class);
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
	}

	@Override
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		serial.writeObject(out, Long.class, length());
		FileInputStream in = new FileInputStream(this);
		Streams.copy(in, out);
		in.close();
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		long length = serial.readObject(in, Long.class);
		FileOutputStream out = new FileOutputStream(this);
		Streams.copy(in, out, length);
		out.close();
	}
}
