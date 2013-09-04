package org.badiff;

import java.io.IOException;
import java.io.InputStream;

public interface BADiffer {
	public BADiff computeDiff(InputStream orig, InputStream target) throws IOException;
}
