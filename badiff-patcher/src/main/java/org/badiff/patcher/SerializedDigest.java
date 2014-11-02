package org.badiff.patcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.util.Digests;

public class SerializedDigest implements Serialized {
	public static final SerializedDigest DEFAULT_ZEROES = new SerializedDigest(Digests.DEFAULT_ALGORITHM, Digests.defaultZeroes());
	
	private String algorithm;
	private byte[] digest;
	
	public SerializedDigest() {}
	
	public SerializedDigest(String algorithm, File content) throws IOException {
		if(content == null)
			throw new IllegalArgumentException();
		this.algorithm = algorithm;
		this.digest = Digests.digest(content, Digests.digest(algorithm));
	}
	
	public SerializedDigest(String algorithm, String content) {
		this(algorithm, Digests.digest(algorithm).digest(content.getBytes(Charset.forName("UTF-8"))));
	}
	
	public SerializedDigest(String algorithm, byte[] digest) {
		if(algorithm == null || digest == null)
			throw new IllegalArgumentException();
		this.algorithm = algorithm;
		this.digest = digest;
	}
	
	public SerializedDigest(String asString) {
		String[] f = asString.split("_", 2);
		if(f.length == 1) {
			algorithm = Digests.DEFAULT_ALGORITHM;
			digest = Digests.parse(f[0]);
		} else {
			algorithm = f[0];
			digest = Digests.parse(f[1]);
		}
	}

	@Override
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		serial.writeObject(out, String.class, algorithm);
		serial.writeObject(out, byte[].class, digest);
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		algorithm = serial.readObject(in, String.class);
		digest = serial.readObject(in, byte[].class);
	}
	
	@Override
	public String toString() {
		return (Digests.DEFAULT_ALGORITHM.equals(algorithm) ? "" : algorithm + "_") + Digests.pretty(digest);
	}
	
	public String getAlgorithm() {
		return algorithm;
	}
	
	public byte[] getDigest() {
		return digest;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof SerializedDigest) {
			SerializedDigest other = (SerializedDigest) obj;
			return algorithm.equals(other.algorithm) && Arrays.equals(digest, other.digest);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(digest);
	}
}
