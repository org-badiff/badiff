package org.badiff.io;

import java.io.File;
import java.net.URI;

import org.badiff.imp.FileDiff;

/**
 * {@link FileDiff} that uses {@link JdkSerialization}
 * @author robin
 *
 */
public class DefaultFileDiff extends FileDiff {
	private static final long serialVersionUID = 0;

	public DefaultFileDiff(File parent, String child) {
		super(parent, child);
	}

	public DefaultFileDiff(String parent, String child) {
		super(parent, child);
	}

	public DefaultFileDiff(String pathname) {
		super(pathname);
	}

	public DefaultFileDiff(URI uri) {
		super(uri);
	}

	public DefaultFileDiff(File file) {
		super(file);
	}

	@Override
	protected Serialization serialization() {
		return DefaultSerialization.getInstance();
	}

}
