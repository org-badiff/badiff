package org.badiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public interface Diff {
	public final int DEFAULT_CHUNK = 1024;
	
	public void applyDiff(InputStream orig, OutputStream target) throws IOException;
	public void storeDiff(Iterator<Op> ops) throws IOException;
}
