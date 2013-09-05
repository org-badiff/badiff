package org.badiff.kryo;

import java.io.File;
import java.io.ObjectStreamException;
import java.net.URI;

import org.badiff.imp.FileDiff;
import org.badiff.io.Serialization;

public class KryoFileDiff extends FileDiff {
	private static final long serialVersionUID = 0;
	
	private transient KryoSerialization kryo = new KryoSerialization();
	
	public KryoFileDiff(File parent, String child) {
		super(parent, child);
	}

	public KryoFileDiff(String parent, String child) {
		super(parent, child);
	}

	public KryoFileDiff(String pathname) {
		super(pathname);
	}

	public KryoFileDiff(URI uri) {
		super(uri);
	}

	public KryoFileDiff(File file) {
		super(file);
	}

	@Override
	protected Serialization serialization() {
		return kryo;
	}
	
	public KryoFileDiff stripDeletes(boolean strip) {
		kryo.stripDeletes(strip);
		return this;
	}

	public boolean stripDeletes() {
		return kryo.stripDeletes();
	}
	
	private Object readResolve() throws ObjectStreamException {
		kryo = new KryoSerialization();
		return this;
	}
}
