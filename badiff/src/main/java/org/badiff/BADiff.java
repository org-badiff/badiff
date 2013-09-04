package org.badiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BADiff {
	public void applyTo(InputStream orig, OutputStream target) throws IOException;
	public byte[] applyTo(byte[] orig);
	public long targetLength();
}
