package org.badiff.patcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.util.Digests;

public class PathDigest implements Serialized {
	protected String path;
	protected SerializedDigest digest;
	
	public PathDigest() {}
	
	public PathDigest(String path, SerializedDigest digest) {
		if(path == null || digest == null)
			throw new IllegalArgumentException();
		this.path = path;
		this.digest = digest;
	}
	
	public SerializedDigest getDigest() {
		return digest;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getPrefix() {
		return new SerializedDigest(Digests.DEFAULT_ALGORITHM, path).toString();
	}

	@Override
	public void serialize(Serialization serial, DataOutput out)
			throws IOException {
		serial.writeObject(out, String.class, path);
		serial.writeObject(out, SerializedDigest.class, digest);
	}

	@Override
	public void deserialize(Serialization serial, DataInput in)
			throws IOException {
		path = serial.readObject(in, String.class);
		digest = serial.readObject(in, SerializedDigest.class);
	}
	
	@Override
	public int hashCode() {
		return digest.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof PathDigest) {
			PathDigest other = (PathDigest) obj;
			return path.equals(other.path) && digest.equals(other.digest);
		}
		return false;
	}
}
