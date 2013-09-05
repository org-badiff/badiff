package org.badiff.patch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;

import org.badiff.io.Serialization;
import org.badiff.io.Serialized;

/**
 * Implementation of {@link Patch} that lives entirely in memory, backed
 * by a {@link TreeMap}
 * @author robin
 *
 */
public class MemoryPatch extends TreeMap<String, PatchOp> implements Patch, Serialized {
	private static final long serialVersionUID = 0;

	@Override
	public void apply(File orig, File target) throws IOException {
		for(String path : keySet()) {
			PatchOp op = get(path);
			op.apply(new File(orig, path), new File(target, path));
		}
	}
	
	@Override
	public void apply(File orig) throws IOException {
		for(String path : keySet()) {
			PatchOp op = get(path);
			op.apply(new File(orig, path));
		}
	}
	
	@Override
	public boolean containsKey(String path) {
		return containsKey((Object) path);
	}

	@Override
	public PatchOp get(String path) {
		return get((Object) path);
	}

	@Override
	public void store(Patch other) {
		clear();
		for(String path : other.keySet())
			put(path, other.get(path));
	}

	@Override
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		serial.writeObject(out, Integer.class, size());
		for(String path : keySet()) {
			serial.writeObject(out, String.class, path);
			serial.writeObject(out, PatchOp.class, get(path));
		}
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		int size = serial.readObject(in, Integer.class);
		for(int i = 0; i < size; i++) {
			String path = serial.readObject(in, String.class);
			PatchOp op = serial.readObject(in, PatchOp.class);
			put(path, op);
		}
	}

}
