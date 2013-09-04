package org.badiff.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.badiff.BADiff;
import org.badiff.Op;

public abstract class DiffFile extends File implements BADiff {
	
	protected abstract Serialization serialization();

	public DiffFile(File parent, String child) {
		super(parent, child);
	}

	public DiffFile(String parent, String child) {
		super(parent, child);
	}

	public DiffFile(String pathname) {
		super(pathname);
	}

	public DiffFile(URI uri) {
		super(uri);
	}

	public DiffFile(File file) {
		this(file.toURI());
	}
	
	@Override
	public void applyDiff(InputStream orig, OutputStream target)
			throws IOException {
		InputStream self = new FileInputStream(this);
		try {
			long count = serialization().readObject(self, Long.class);
			for(long i = 0; i < count; i++)
				serialization().readObject(self, Op.class).applyOp(orig, target);
		} finally { 
			self.close();
		}
	}
}
