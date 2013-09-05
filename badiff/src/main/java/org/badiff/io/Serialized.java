package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Interface for objects which can be serialized and deserialized with a {@link Serialization}
 * @author robin
 *
 */
public interface Serialized extends Serializable {
	/**
	 * Serialize this object
	 * @param serial
	 * @param out
	 * @throws IOException
	 */
	public void serialize(Serialization serial, OutputStream out) throws IOException;
	/**
	 * Deserialize this object
	 * @param serial
	 * @param in
	 * @throws IOException
	 */
	public void deserialize(Serialization serial, InputStream in) throws IOException;
}
