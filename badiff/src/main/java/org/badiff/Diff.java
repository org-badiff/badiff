package org.badiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Diff {
	public void applyDiff(InputStream orig, OutputStream target) throws IOException;
}
