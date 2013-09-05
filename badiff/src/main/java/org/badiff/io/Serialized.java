package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public interface Serialized extends Serializable {
	public void serialize(Serialization serial, OutputStream out) throws IOException;
	public void deserialize(Serialization serial, InputStream in) throws IOException;
}
