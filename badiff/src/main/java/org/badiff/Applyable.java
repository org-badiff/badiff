package org.badiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Applyable {
	public void apply(InputStream orig, OutputStream target) throws IOException;

}
