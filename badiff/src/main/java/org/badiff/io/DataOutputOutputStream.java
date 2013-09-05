package org.badiff.io;

import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * {@link OutputStream} that reads from an {@link ObjectOutput}
 * @author robin
 *
 */
public class DataOutputOutputStream extends OutputStream {

	protected DataOutput out;
	
	public DataOutputOutputStream(DataOutput out) {
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

}
