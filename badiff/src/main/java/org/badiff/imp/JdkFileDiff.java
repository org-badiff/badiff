package org.badiff.imp;

import java.io.File;
import java.net.URI;

import org.badiff.io.JdkSerialization;
import org.badiff.io.Serialization;

/**
 * {@link FileDiff} that uses {@link JdkSerialization}
 * @author robin
 *
 */
public class JdkFileDiff extends FileDiff {
	private static final long serialVersionUID = 0;

	public JdkFileDiff(File parent, String child) {
		super(parent, child);
	}

	public JdkFileDiff(String parent, String child) {
		super(parent, child);
	}

	public JdkFileDiff(String pathname) {
		super(pathname);
	}

	public JdkFileDiff(URI uri) {
		super(uri);
	}

	public JdkFileDiff(File file) {
		super(file);
	}

	@Override
	protected Serialization serialization() {
		return JdkSerialization.getInstance();
	}

}
