package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

public class ObjectInputInputStream extends InputStream {
	
	protected ObjectInput in;

	public ObjectInputInputStream(ObjectInput in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

}
