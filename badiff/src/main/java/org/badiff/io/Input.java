package org.badiff.io;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

/**
 * A random-access {@link DataInput}
 * @author robin
 *
 */
public interface Input extends DataInput {
	/**
	 * Returns the first position that can be {@link #seek(long)}ed
	 * @return
	 */
	public long first();
	/**
	 * Returns the last position, plus one, that can be {@link #seek(long)}ed
	 * @return
	 */
	public long last();
	/**
	 * Returns the current position
	 * @return
	 */
	public long position();
	/**
	 * Set the current position
	 * @param pos
	 * @throws IOException
	 */
	public void seek(long pos) throws IOException;
	
	/**
	 * @see InputStream#skip(long)
	 * @param count
	 * @throws IOException
	 */
	public void skip(long count) throws IOException;
	
	public int read() throws IOException;
	
	public int read(byte[] b) throws IOException;
	
	public int read(byte[] b, int off, int len) throws IOException;
}
