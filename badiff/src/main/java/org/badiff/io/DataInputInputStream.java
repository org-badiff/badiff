package org.badiff.io;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

/**
 * {@link InputStream} that reads from an {@link ObjectInput}
 * @author robin
 *
 */
public class DataInputInputStream extends InputStream {
	
	protected DataInput in;

	public DataInputInputStream(DataInput in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		try {
			return 0xff & in.readByte();
		} catch(EOFException eof) {
			return -1;
		}
	}

}
