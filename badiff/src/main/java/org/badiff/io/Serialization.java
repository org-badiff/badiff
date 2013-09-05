package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A mechanism for serializing data to/from {@link OutputStream} / {@link InputStream}
 * @author robin
 *
 */
public interface Serialization {
	/**
	 * Write the argument object to the {@link OutputStream}.
	 * The object may be null.
	 * @param out
	 * @param type
	 * @param object
	 * @throws IOException
	 */
	public <T> void writeObject(OutputStream out, Class<T> type, T object) throws IOException;
	
	/**
	 * Read an object of the argument type from the {@link InputStream}
	 * @param in
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public <T> T readObject(InputStream in, Class<T> type) throws IOException;
}
