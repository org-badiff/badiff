package org.badiff.patcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	@Override
	public String toString() {
		return from + "->" + to;
	}
	
	protected PathDiff(String name) {
		String[] fields = name.split("\\.");
		ts = new BigInteger(fields[0], 16).longValue();
		pathId = new SerializedDigest(fields[1]);
		from = new SerializedDigest(fields[2]);
		to = new SerializedDigest(fields[3]);
	}
	
	public PathDiff(String name, BadiffFileDiff diff) {
		this(name);
		this.diff = diff;
	}
	
	public PathDiff(long ts, String path, SerializedDigest from, SerializedDigest to, BadiffFileDiff diff) throws IOException {
		BadiffFileDiff.Header.Optional opt;
		if(path == null || diff == null || (opt = diff.header().getOptional()) == null)
			throw new IllegalArgumentException();
		
		this.ts = ts;
		this.path = path;
		this.from = from;
		this.to = to;
		this.diff = diff;
		pathId = new SerializedDigest(Digests.DEFAULT_ALGORITHM, path);
	}
	
	protected PathDiff(String path, long ts, SerializedDigest from, SerializedDigest to) {
		this.path = path;
		this.ts = ts;
		this.pathId = new SerializedDigest(Digests.DEFAULT_ALGORITHM, path);
		this.from = from;
		this.to = to;
	}
	
	protected PathDiff(SerializedDigest pathId, long ts, SerializedDigest from, SerializedDigest to) {
		this.pathId = pathId;
		this.ts = ts;
		this.from = from;
		this.to = to;
	}
	
	public PathDiff reverse() {
		return new PathDiff(pathId, ts, to, from);
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
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		serial.writeObject(out, Long.class, ts);
		serial.writeObject(out, String.class, path);
		serial.writeObject(out, SerializedDigest.class, from);
		serial.writeObject(out, SerializedDigest.class, to);
		diff.serialize(serial, out);
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		ts = serial.readObject(in, Long.class);
		path = serial.readObject(in, String.class);
		from = serial.readObject(in, SerializedDigest.class);
		to = serial.readObject(in, SerializedDigest.class);
		File root = (File) serial.graphContext().get(DESERIALIZE_ROOT);
		diff = new BadiffFileDiff(root, getName());
		diff.deserialize(serial, in);
	}
}
