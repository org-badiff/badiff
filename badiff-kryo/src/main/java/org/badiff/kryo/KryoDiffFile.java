package org.badiff.kryo;

import java.io.File;
import java.net.URI;

import org.badiff.io.DiffFile;
import org.badiff.io.Serialization;

public class KryoDiffFile extends DiffFile {
	private KryoSerialization kryo = new KryoSerialization();
	
	public KryoDiffFile(File parent, String child) {
		super(parent, child);
	}

	public KryoDiffFile(String parent, String child) {
		super(parent, child);
	}

	public KryoDiffFile(String pathname) {
		super(pathname);
	}

	public KryoDiffFile(URI uri) {
		super(uri);
	}

	public KryoDiffFile(File file) {
		super(file);
	}

	@Override
	protected Serialization serialization() {
		return kryo;
	}
	
	public KryoDiffFile stripDeletes(boolean strip) {
		kryo.stripDeletes(strip);
		return this;
	}

	public boolean stripDeletes() {
		return kryo.stripDeletes();
	}
}
