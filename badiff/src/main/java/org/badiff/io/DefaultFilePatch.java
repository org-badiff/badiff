package org.badiff.io;

import java.io.File;
import java.net.URI;

import org.badiff.imp.FilePatch;

/**
 * {@link FilePatch} that uses {@link JdkSerialization}
 * @author robin
 *
 */
public class DefaultFilePatch extends FilePatch {
	private static final long serialVersionUID = 0;

	public DefaultFilePatch(String pathname) {
		super(pathname);
	}

	public DefaultFilePatch(URI uri) {
		super(uri);
	}

	public DefaultFilePatch(String parent, String child) {
		super(parent, child);
	}

	public DefaultFilePatch(File parent, String child) {
		super(parent, child);
	}

	public DefaultFilePatch(File file) {
		super(file);
	}

	@Override
	protected Serialization serialization() {
		return DefaultSerialization.getInstance();
	}

}
