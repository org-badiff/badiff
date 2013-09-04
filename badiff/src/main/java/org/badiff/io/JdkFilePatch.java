package org.badiff.io;

import java.io.File;
import java.net.URI;

public class JdkFilePatch extends FilePatch {
	private static final long serialVersionUID = 0;

	public JdkFilePatch(String pathname) {
		super(pathname);
	}

	public JdkFilePatch(URI uri) {
		super(uri);
	}

	public JdkFilePatch(String parent, String child) {
		super(parent, child);
	}

	public JdkFilePatch(File parent, String child) {
		super(parent, child);
	}

	public JdkFilePatch(File file) {
		super(file);
	}

	@Override
	protected Serialization serialization() {
		return JdkSerialization.getInstance();
	}

}
