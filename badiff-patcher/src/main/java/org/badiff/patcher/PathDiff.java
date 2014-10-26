package org.badiff.patcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.util.Digests;

public class PathDiff implements Serialized {
	public static final Object DESERIALIZE_ROOT = new Object();
	
	protected String path;
	protected BadiffFileDiff diff;
	protected SerializedDigest from;
	protected SerializedDigest to;
	
	public PathDiff() {}
	
	public PathDiff(String path, BadiffFileDiff diff) throws IOException {
		BadiffFileDiff.Header.Optional opt;
		if(path == null || diff == null || (opt = diff.header().getOptional()) == null)
			throw new IllegalArgumentException();
		
		this.path = path;
		this.diff = diff;
		from = new SerializedDigest(opt.getHashAlgorithm(), opt.getPreHash());
		to = new SerializedDigest(opt.getHashAlgorithm(), opt.getPostHash());
	}
	
	public String getPath() {
		return path;
	}
	
	public BadiffFileDiff getDiff() {
		return diff;
	}
	
	public SerializedDigest getFrom() {
		return from;
	}
	
	public SerializedDigest getTo() {
		return to;
	}
	
	public String getName() {
		return new SerializedDigest(Digests.DEFAULT_ALGORITHM, path) + "." + from + "." + to + ".badiff";
	}

	@Override
	public void serialize(Serialization serial, DataOutput out)
			throws IOException {
		serial.writeObject(out, String.class, path);
		serial.writeObject(out, SerializedDigest.class, from);
		serial.writeObject(out, SerializedDigest.class, to);
		diff.serialize(serial, out);
	}

	@Override
	public void deserialize(Serialization serial, DataInput in)
			throws IOException {
		path = serial.readObject(in, String.class);
		from = serial.readObject(in, SerializedDigest.class);
		to = serial.readObject(in, SerializedDigest.class);
		File root = (File) serial.graphContext().get(DESERIALIZE_ROOT);
		diff = new BadiffFileDiff(root, getName());
		diff.deserialize(serial, in);
	}
}
