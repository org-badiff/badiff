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
import java.security.DigestOutputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.io.DataOutputOutputStream;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.FileRandomInput;
import org.badiff.io.Random;
import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.io.SmallNumberSerialization;
import org.badiff.q.CoalescingOpQueue;
import org.badiff.q.CompactingOpQueue;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.ParallelGraphOpQueue;
import org.badiff.q.PumpingOpQueue;
import org.badiff.q.RewindingOpQueue;
import org.badiff.q.StreamChunkingOpQueue;
import org.badiff.q.UnchunkingOpQueue;
import org.badiff.util.Digests;
import org.badiff.util.Streams;

/**
 * A diff file using the badiff file format.
 * Contains metadata in addition to the raw {@link Op}s found in {@link FileDiff}
 * @author robin
 *
 */
public class BadiffFileDiff extends File implements Diff, Serialized {
	private static final long serialVersionUID = 0;

	/**
	 * Magic bytes at the beginning of every badiff file
	 */
	/*
	 * DEEEEEEEEEEEEEEEEEEEEEEF
	 */
	public static final byte[] MAGIC = new byte[] {0, (byte)0xde, (byte)0xee, (byte)0xef};
	/**
	 * badiff file format version
	 */
	public static final int VERSION = 1;

	/**
	 * Flag to indicate that this diff can only be applied to an {@link InputStream}
	 * that also implements {@link Random}
	 */
	public static final long FLAG_RANDOM_ACCESS = 0x1;
	/**
	 * Flag that this badiff diff uses the {@link DefaultSerialization}
	 */
	public static final long FLAG_DEFAULT_SERIALIZATION = 0x2;
	/**
	 * Flag that this badiff diff uses the {@link SmallNumberSerialization}
	 */
	public static final long FLAG_SMALL_NUMBER_SERIALIZATION = 0x4;
	/**
	 * Flag that this badiff diff uses an unknown serialization, which must be supplied
	 * to the constructor
	 */
	public static final long FLAG_UNSPECIFIED_SERIALIZATION = 0x8;
	/**
	 * Flag that this badiff diff has an optional data section
	 */
	public static final long FLAG_OPTIONAL_DATA = 0x10;
	
	/**
	 * A header found at the beginning of every badiff diff
	 * @author robin
	 *
	 */
	public static class Header {
		/**
		 * Statistics summarizing this badiff diff
		 * @author robin
		 *
		 */
		public class Stats implements Serialized {
			/**
			 * The number of rewinds (DELETE with negative run length)
			 */
			private long rewindCount;
			/**
			 * The number of NEXT operations
			 */
			private long nextCount;
			/**
			 * The number of INSERT operations
			 */
			private long insertCount;
			/**
			 * The number of DELETE operations
			 */
			private long deleteCount;
			/**
			 * The size of the input file
			 */
			private long inputSize;
			/**
			 * The size of the output file
			 */
			private long outputSize;
			
			private Stats() {}
			
			@Override
			public void serialize(Serialization serial, OutputStream out)
					throws IOException {
				serial.writeObject(out, Long.class, rewindCount);
				serial.writeObject(out, Long.class, nextCount);
				serial.writeObject(out, Long.class, insertCount);
				serial.writeObject(out, Long.class, deleteCount);
				serial.writeObject(out, Long.class, inputSize);
				serial.writeObject(out, Long.class, outputSize);
			}
			@Override
			public void deserialize(Serialization serial, InputStream in)
					throws IOException {
				rewindCount = serial.readObject(in, Long.class);
				nextCount = serial.readObject(in, Long.class);
				insertCount = serial.readObject(in, Long.class);
				deleteCount = serial.readObject(in, Long.class);
				inputSize = serial.readObject(in, Long.class);
				outputSize = serial.readObject(in, Long.class);
			}
		
			/**
			 * Returns the number of rewinds (DELETE with negative run length)
			 * @return
			 */
			public long getRewindCount() {
				return rewindCount;
			}
		
			/**
			 * Returns the total number of NEXT operations
			 * @return
			 */
			public long getNextCount() {
				return nextCount;
			}
		
			/**
			 * Returns the total number of INSERT operations
			 * @return
			 */
			public long getInsertCount() {
				return insertCount;
			}
		
			/**
			 * Returns the total number of DELETE operations
			 * @return
			 */
			public long getDeleteCount() {
				return deleteCount;
			}
		
			/**
			 * Returns the expected input size
			 * @return
			 */
			public long getInputSize() {
				return inputSize;
			}
		
			/**
			 * Returns the expected output size
			 * @return
			 */
			public long getOutputSize() {
				return outputSize;
			}
		}

		/**
		 * Optional data for a diff.  Any field may be null.
		 * @author robin
		 *
		 */
		public class Optional implements Serialized {
			public Optional() {}
			
			/**
			 * The expected hash of the input file
			 */
			private byte[] preHash;
			/**
			 * The expected hash of the output file
			 */
			private byte[] postHash;
			/**
			 * The algorithm used to compute hashes
			 */
			private String hashAlgorithm;
			
			/**
			 * Returns the expected hash of the input file
			 * @return
			 */
			public byte[] getPreHash() {
				return preHash;
			}
			/**
			 * Sets the expected hash of the input file
			 * @param preHash
			 */
			public void setPreHash(byte[] preHash) {
				this.preHash = preHash;
			}
			/**
			 * Returns the expected hash of the output file
			 * @return
			 */
			public byte[] getPostHash() {
				return postHash;
			}
			/**
			 * Sets the expected hash of the output file
			 * @param postHash
			 */
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
			/**
			 * Returns the hashing algorithm used
			 * @return
			 */
			public String getHashAlgorithm() {
				return hashAlgorithm;
			}
			/**
			 * Sets the hashing algorithm used
			 * @param hashAlgorithm
			 */
			public void setHashAlgorithm(String hashAlgorithm) {
				this.hashAlgorithm = hashAlgorithm;
			}
		}

		/**
		 * The magic bytes
		 */
		private byte[] magic;
		/**
		 * The version
		 */
		private int version;
		/**
		 * The flags
		 */
		private long flags;
		/**
		 * The {@link Serialization} used for this diff
		 */
		private Serialization serial;
		/**
		 * Statistics about this diff
		 */
		private Header.Stats stats = new Stats();
		/**
		 * Optional data (may be null) for this diff
		 */
		private Header.Optional optional = null;
		
		public Header() {}
		
		public Header(long flags, Serialization serial) {
			this.flags = flags;
			this.serial = serial;
		}

		/**
		 * Returns the magic bytes for this diff
		 * @return
		 */
		public byte[] getMagic() {
			return magic;
		}

		/**
		 * Returns the version for this diff
		 * @return
		 */
		public int getVersion() {
			return version;
		}

		/**
		 * Returns the flags for this diff
		 * @return
		 */
		public long getFlags() {
			return flags;
		}

		/**
		 * Returns the {@link Serialization} used by this diff
		 * @return
		 */
		public Serialization getSerial() {
			return serial;
		}

		/**
		 * Returns statistics about this diff
		 * @return
		 */
		public Header.Stats getStats() {
			return stats;
		}
		
		/**
		 * Returns optional data about this diff (may be null)
		 * @return
		 */
		public Header.Optional getOptional() {
			return optional;
		}
		
		/**
		 * Sets the optional data for this header
		 * @param optional
		 */
		public void setOptional(Header.Optional optional) {
			this.optional = optional;
		}
	}
	
	/**
	 * Compute statistics for any {@link Diff}
	 * @param diff
	 * @return
	 * @throws IOException
	 */
	protected static void computeStats(Diff diff, Header header) throws IOException {
		Header.Stats stats = header.stats;
		OpQueue q = diff.queue();
		long osize = 0; // input file size
		long tsize = 0; // output file size
		for(Op e = q.poll(); e != null; e = q.poll()) {
			switch(e.getOp()) {
			case Op.DELETE:
				stats.deleteCount++;
				if(e.getRun() < 0)
					stats.rewindCount++;
				osize += e.getRun();
				break;
				
			case Op.INSERT:
				stats.insertCount++;
				tsize += e.getRun();
				break;
				
			case Op.NEXT:
				stats.nextCount++;
				osize += e.getRun();
				tsize += e.getRun();
				break;
			}
		}
		stats.inputSize = osize;
		stats.outputSize = tsize;
	}
	
	/**
	 * The {@link Serialization} for this diff.  If null when diffing, {@link DefaultSerialization} is used.
	 * If null when applying, the {@link Serialization} is read from the {@link Header}'s flags.
	 */
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

	/**
	 * Write a badiff diff header
	 * @param serial
	 * @param stats
	 * @param opt
	 * @param out
	 * @throws IOException
	 */
	public static void writeHeader(Header header, DataOutput out) throws IOException {
		long flags;
		
		if(header.flags != 0)
			flags = header.flags;
		else {
			flags = 0;
			if(header.stats.rewindCount > 0)
				flags |= FLAG_RANDOM_ACCESS;

			if(header.serial instanceof DefaultSerialization)
				flags |= FLAG_DEFAULT_SERIALIZATION;
			else if(header.serial instanceof SmallNumberSerialization)
				flags |= FLAG_SMALL_NUMBER_SERIALIZATION;
			else
				flags |= FLAG_UNSPECIFIED_SERIALIZATION;

			if(header.optional != null)
				flags |= FLAG_OPTIONAL_DATA;
		}
		
		out.write(MAGIC);
		out.writeInt(VERSION);
		out.writeLong(flags);
		
		DataOutputOutputStream dout = new DataOutputOutputStream(out);
		
		header.stats.serialize(header.serial, dout);
		
		if(header.optional != null)
			header.optional.serialize(header.serial, dout);
	}
	
	/**
	 * Read a badiff diff header
	 * @param in
	 * @param serial
	 * @return
	 * @throws IOException
	 */
	public static Header readHeader(DataInputStream in, Serialization serial) throws IOException {
		Header header = new Header();
		
		byte[] magic = new byte[MAGIC.length];
		in.read(magic);
		if(!Arrays.equals(magic, MAGIC))
			throw new IOException("Invalid badiff magic");
		
		int version = in.readInt();
		if(version < 1 || version > VERSION)
			throw new IOException("Unrecognized version");
		
		long flags = in.readLong();
		
		if((flags & FLAG_DEFAULT_SERIALIZATION) != 0) {
			if(serial != null && !(serial instanceof DefaultSerialization))
				throw new IOException(
						"Incompatible serialization; expected " 
								+ serial.getClass().getSimpleName() + ", file declares " 
								+ DefaultSerialization.class.getSimpleName());
			else if(serial == null)
				serial = DefaultSerialization.newInstance();
		}
		if((flags & FLAG_SMALL_NUMBER_SERIALIZATION) != 0) {
			if(serial != null && !(serial instanceof SmallNumberSerialization))
				throw new IOException(
						"Incompatible serialization; expected " 
								+ serial.getClass().getSimpleName() + ", file declares " 
								+ SmallNumberSerialization.class.getSimpleName());
			else if(serial == null)
				serial = SmallNumberSerialization.newInstance();
		}
		if((flags & FLAG_UNSPECIFIED_SERIALIZATION) != 0) {
			if(serial == null)
				throw new IOException("Incompatible serialization; expected file to specify, file declares unspecified");
		}
		
		Header.Stats stats = header.stats;
		stats.deserialize(serial, in);
		
		Header.Optional opt = null;
		if((flags & FLAG_OPTIONAL_DATA) != 0) {
			opt = header.new Optional();
			opt.deserialize(serial, in);
		}
		
		header.magic = magic;
		header.version = version;
		header.flags = flags;
		header.serial = serial;
		header.stats = stats;
		header.optional = opt;
		
		return header;
	}

	/**
	 * Returns the header for this badiff diff
	 * @return
	 * @throws IOException
	 */
	public Header header() throws IOException {
		DataInputStream in = new DataInputStream(new FileInputStream(this));
		Header header = readHeader(in, serial);
		in.close();
		return header;
	}
	
	/**
	 * Returns statistics about this badiff diff
	 * @return
	 * @throws IOException
	 */
	public Header.Stats stats() throws IOException {
		return header().stats;
	}
	
	/**
	 * Compute a diff from {@code orig} to {@code target} and store in this badiff diff.
	 * The computed diff is one-way and uses rewind optimization.
	 * @param orig
	 * @param target
	 * @throws IOException
	 */
	public void diff(File orig, File target) throws IOException {
		byte[] preHash = Digests.digest(orig, Digests.defaultDigest());
		byte[] postHash = Digests.digest(target, Digests.defaultDigest());
		
		FileDiff tmp = new FileDiff(getParentFile(), getName() + ".tmp");
		
		InputStream oin = new FileRandomInput(orig);
		InputStream tin = new FileRandomInput(target);
		
		OpQueue q;
		q = new StreamChunkingOpQueue(oin, tin);
		q = new ParallelGraphOpQueue(q, ParallelGraphOpQueue.INERTIAL_GRAPH);
		q = new CoalescingOpQueue(q);
		q = new CoalescingOpQueue(q);
		q = new RewindingOpQueue(q);
		q = new OneWayOpQueue(q);
		q = new UnchunkingOpQueue(q);
		q = new CompactingOpQueue(q);
		tmp.store(q);
		
		Header h = new Header();
		
		Header.Optional opt = h.optional = h.new Optional();
		opt.setHashAlgorithm(Digests.defaultDigest().getAlgorithm());
		opt.setPreHash(preHash);
		opt.setPostHash(postHash);
		
		DataOutputStream self = new DataOutputStream(new FileOutputStream(this));
		store(self, serial, h, tmp.queue());
		self.close();
		
		tmp.delete();
		tin.close();
		oin.close();
	}
	
	/**
	 * Apply this diff to the {@code orig} File, (over)writing the {@code target} File.
	 * @param orig
	 * @param target
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void apply(File orig, File target) throws IOException {
		Header header = header();
		Header.Optional opt = header.getOptional();
		
		byte[] expectedPreHash = null;
		byte[] expectedPostHash = null;
		if(opt != null) {
			if(opt.getHashAlgorithm() != null && opt.getPreHash() != null)
				expectedPreHash = opt.getPreHash();
			if(opt.getHashAlgorithm() != null && opt.getPostHash() != null)
				expectedPostHash = opt.getPostHash();
		}
		
		if(expectedPreHash != null) {
			byte[] actualPreHash = Digests.digest(orig, Digests.digest(opt.getHashAlgorithm()));
			if(!Arrays.equals(expectedPreHash, actualPreHash))
				throw new IOException(
						"Hash mismatch on original, expected " 
								+ Digests.pretty(expectedPreHash) + ", actual " 
								+ Digests.pretty(actualPreHash));
		}
		
		File tmp = new File(target.getParentFile(), target.getName() + ".patching");
		OutputStream out = new FileOutputStream(tmp);
		
		DigestOutputStream digout = null;
		if(expectedPostHash != null) {
			digout = new DigestOutputStream(out, Digests.digest(opt.getHashAlgorithm()));
			out = digout;
		}
		
		InputStream oin = new FileRandomInput(orig);
		apply(oin, out);
		out.close();
		
		if(digout != null) {
			byte[] actualPostHash = digout.getMessageDigest().digest();
			if(!Arrays.equals(expectedPostHash, actualPostHash))
				throw new IOException(
						"Hash mismatch on target, expected " 
								+ Digests.pretty(expectedPostHash) + ", actual " 
								+ Digests.pretty(actualPostHash));
		}
		
		target.delete();
		tmp.renameTo(target);
	}
	
	@Override
	public void apply(InputStream orig, OutputStream target) throws IOException {
		Header header = header();
		if((header.flags & FLAG_RANDOM_ACCESS) != 0 && !(orig instanceof Random))
			throw new IOException(this + " requires a random-access original (" + Random.class + ")");
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
	
	/**
	 * Write a badiff file
	 * @param out
	 * @param serial
	 * @param opt
	 * @param ops
	 * @throws IOException
	 */
	public static void store(DataOutput out, Serialization serial, Header header, Iterator<Op> ops) throws IOException {
		/* 
		 * shove the ops into a temp FileDiff first so we can compute some stats
		 * without having them all in memory
		 */
		FileDiff tmp = new FileDiff(File.createTempFile("filediff", ".tmp"));
		tmp.store(ops);
		
		// Compute the stats
		computeStats(tmp, header);
		
		if(serial == null)
			serial = DefaultSerialization.newInstance();
		header.serial = serial;
		
		// Write the header
		writeHeader(header, out);
		
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
	
	/**
	 * {@link OpQueue} backed by this badiff file
	 * @author robin
	 *
	 */
	private class FileBadiffOpQueue extends OpQueue {
		private Header header;
		private DataInputStream self;
		private boolean closed;
		
		public FileBadiffOpQueue() throws IOException {
			self = new DataInputStream(new FileInputStream(BadiffFileDiff.this));
			header = readHeader(self, BadiffFileDiff.this.serial);
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
		
		@Override
		public String toString() {
			return getName();
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
