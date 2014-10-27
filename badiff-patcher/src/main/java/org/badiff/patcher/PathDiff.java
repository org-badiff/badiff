package org.badiff.patcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Comparator;

import org.badiff.imp.BadiffFileDiff;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.util.Digests;

public class PathDiff implements Serialized {
	public static final Object DESERIALIZE_ROOT = new Object();
	
	public static final Comparator<PathDiff> TS_ORDER = new Comparator<PathDiff>() {
		@Override
		public int compare(PathDiff o1, PathDiff o2) {
			return ((Long) o1.getTs()).compareTo(o2.getTs());
		}
	};
	
	public static String getName(long ts, SerializedDigest pathId, SerializedDigest from, SerializedDigest to) {
		return String.format("%016x.%s.%s.%s.badiff", ts, pathId, from, to);
	}
	
	public static PathDiff parseName(String name) {
		return new PathDiff(name);
	}
	
	protected String path;
	protected long ts;
	protected BadiffFileDiff diff;
	protected SerializedDigest pathId;
	protected SerializedDigest from;
	protected SerializedDigest to;
	
	public PathDiff() {}
	
	protected PathDiff(String name) {
		String[] fields = name.split("\\.");
		ts = new BigInteger(fields[0], 16).longValue();
		pathId = new SerializedDigest(fields[1]);
		from = new SerializedDigest(fields[2]);
		to = new SerializedDigest(fields[3]);
	}
	
	public PathDiff(long ts, String path, BadiffFileDiff diff) throws IOException {
		BadiffFileDiff.Header.Optional opt;
		if(path == null || diff == null || (opt = diff.header().getOptional()) == null)
			throw new IllegalArgumentException();
		
		this.ts = ts;
		this.path = path;
		this.diff = diff;
		pathId = new SerializedDigest(Digests.DEFAULT_ALGORITHM, path);
		from = new SerializedDigest(opt.getHashAlgorithm(), opt.getPreHash());
		to = new SerializedDigest(opt.getHashAlgorithm(), opt.getPostHash());
	}
	
	public String getPath() {
		return path;
	}
	
	public long getTs() {
		return ts;
	}
	
	public BadiffFileDiff getDiff() {
		return diff;
	}
	
	public SerializedDigest getPathId() {
		return pathId;
	}
	
	public SerializedDigest getFrom() {
		return from;
	}
	
	public SerializedDigest getTo() {
		return to;
	}
	
	public String getName() {
		return getName(ts, pathId, from, to);
	}

	@Override
	public void serialize(Serialization serial, DataOutput out)
			throws IOException {
		serial.writeObject(out, long.class, ts);
		serial.writeObject(out, String.class, path);
		serial.writeObject(out, SerializedDigest.class, from);
		serial.writeObject(out, SerializedDigest.class, to);
		diff.serialize(serial, out);
	}

	@Override
	public void deserialize(Serialization serial, DataInput in)
			throws IOException {
		ts = serial.readObject(in, long.class);
		path = serial.readObject(in, String.class);
		from = serial.readObject(in, SerializedDigest.class);
		to = serial.readObject(in, SerializedDigest.class);
		File root = (File) serial.graphContext().get(DESERIALIZE_ROOT);
		diff = new BadiffFileDiff(root, getName());
		diff.deserialize(serial, in);
	}
}
