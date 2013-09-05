package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serialization {
	public <T> void writeObject(OutputStream out, Class<T> type, T object) throws IOException;
	public <T> T readObject(InputStream in, Class<T> type) throws IOException;
}
