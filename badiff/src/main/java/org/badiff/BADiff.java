package org.badiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BADiff {
	public void applyDiff(InputStream orig, OutputStream target) throws IOException;
	public byte[] applyDiff(byte[] orig);
	public long targetLength();
}
