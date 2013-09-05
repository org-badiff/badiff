package org.badiff.kryo;

import java.io.File;
import java.io.ObjectStreamException;
import java.net.URI;

import org.badiff.io.Serialization;
import org.badiff.patch.FilePatch;

public class KryoFilePatch extends FilePatch {
	private static final long serialVersionUID = 0;

	private transient KryoSerialization kryo = new KryoSerialization();
	
	public KryoFilePatch(String pathname) {
		super(pathname);
	}

	public KryoFilePatch(URI uri) {
		super(uri);
	}

	public KryoFilePatch(String parent, String child) {
		super(parent, child);
	}

	public KryoFilePatch(File parent, String child) {
		super(parent, child);
	}

	public KryoFilePatch(File file) {
		super(file);
	}

	@Override
	protected Serialization serialization() {
		return kryo;
	}
	
	private Object readResolve() throws ObjectStreamException {
		kryo = new KryoSerialization();
		return this;
	}
}
