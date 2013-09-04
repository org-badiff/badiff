package org.badiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public interface Diff extends Applyable {
	public final int DEFAULT_CHUNK = 1024;
	
	public void store(Iterator<Op> ops) throws IOException;
}
