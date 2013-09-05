package org.badiff.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.badiff.Patch;
import org.badiff.PatchOp;
import org.badiff.util.Streams;

public abstract class FilePatch extends File implements Patch {
	private static final long serialVersionUID = 0;
	
	protected abstract Serialization serialization();
	
	public FilePatch(String pathname) {
		super(pathname);
	}

	public FilePatch(URI uri) {
		super(uri);
	}

	public FilePatch(String parent, String child) {
		super(parent, child);
	}

	public FilePatch(File parent, String child) {
		super(parent, child);
	}
	
	public FilePatch(File file) {
		this(file.toURI());
	}

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
	public int size() {
		try {
			InputStream in = new FileInputStream(this);
			try {
				return serialization().readObject(in, Integer.class);
			} finally {
				in.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}

	protected Map<String, Long> readOffsets(InputStream in) throws IOException {
		int size = serialization().readObject(in, Integer.class);
		Map<String, Long> offsets = new TreeMap<String, Long>();
		for(int i = 0; i < size; i++) {
			String key = serialization().readObject(in, String.class);
			long offset = serialization().readObject(in, Long.class);
			offsets.put(key, offset);
		}
		return offsets;
	}
	
	@Override
	public Set<String> keySet() {
		try {
			InputStream in = new FileInputStream(this);
			try {
				return readOffsets(in).keySet();
			} finally {
				in.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}

	@Override
	public boolean containsKey(String path) {
		return keySet().contains(path);
	}

	@Override
	public PatchOp get(String path) {
		try {
			InputStream in = new FileInputStream(this);
			try {
				Map<String, Long> offsets = readOffsets(in);
				in.skip(offsets.get(path));
				return serialization().readObject(in, PatchOp.class);
			} finally {
				in.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}

	@Override
	public PatchOp put(String path, PatchOp diff) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void store(Patch other) {
		try {
			Map<String, File> tmps = new TreeMap<String, File>();
			for(String path : other.keySet()) {
				File tmp = File.createTempFile(path.replaceAll(".*/", ""), ".patchop");
				FileOutputStream out = new FileOutputStream(tmp);
				try {
					serialization().writeObject(out, other.get(path));
				} finally {
					out.close();
				}
				tmps.put(path, tmp);
			}
			
			Map<String, Long> offsets = new TreeMap<String, Long>();
			long offset = 0;
			for(String path : tmps.keySet()) {
				offsets.put(path, offset);
				offset += tmps.get(path).length();
			}
			
			FileOutputStream out = new FileOutputStream(this);
			try {
				serialization().writeObject(out, offsets.size());
				for(String path : offsets.keySet()) {
					serialization().writeObject(out, path);
					serialization().writeObject(out, offsets.get(path));
				}

				for(String path : tmps.keySet()) {
					FileInputStream in = new FileInputStream(tmps.get(path));
					try {
						Streams.copy(in, out);
					} finally {
						in.close();
						tmps.get(path).delete();
					}
				}
			} finally {
				out.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}

}
