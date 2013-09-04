package org.badiff;

import java.io.File;
import java.io.IOException;

public interface FileApplyable {
	public void apply(File orig) throws IOException;
	public void apply(File orig, File target) throws IOException;
}
