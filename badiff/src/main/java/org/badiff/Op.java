package org.badiff;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;

/**
 * A single run-length-encoded operation in a {@link Diff}.
 * @author robin
 *
 */
public class Op implements Applyable, Serialized {
	private static final long serialVersionUID = 0;
	
	/**
	 * Stop diffing
	 */
	public static final byte STOP = 0x0;
	/**
	 * Delete some bytes
	 */
	public static final byte DELETE = 0x1;
	/**
	 * Insert some bytes
	 */
	public static final byte INSERT = 0x2;
	/**
	 * Copy some bytes
	 */
	public static final byte NEXT = 0x3;
	
	/**
	 * The operation
	 */
	private byte op;
	/**
	 * The run-length of the operation
	 */
	private int run;
	/**
	 * The data for the operation
	 */
	private byte[] data;
	
	/*
	 * Required for deserialization
	 */
	public Op() {}
	
	/**
	 * Create a new {@link Op}
	 * @param op
	 * @param run
	 * @param data
	 */
	public Op(byte op, int run, byte[] data) {
		if((op & 0x3) != op)
			throw new IllegalArgumentException("invalid op");
		if(run < 0 || data != null && run > data.length)
			throw new IllegalArgumentException("invalid run");
		if(op == INSERT && data == null)
			throw new IllegalArgumentException("invalid data");
		this.op = op;
		this.run = run;
		this.data = data;
	}

	@Override
	public void apply(InputStream orig, OutputStream target) throws IOException {
		switch(op) {
		case DELETE:
			orig.skip(run);
			break;
		case NEXT:
			int count = run;
			byte[] buf = new byte[Math.min(count, 8192)];
			int r;
			for(r = orig.read(buf); r != -1 && count > 0; r = orig.read(buf)) {
				target.write(buf, 0, r);
				count -= r;
			}
			if(r == -1)
				throw new EOFException();
			break;
		case INSERT:
			target.write(data, 0, run);
			break;
		}
	}

	/**
	 * Returns the operation, one of {@link #STOP}, {@link #DELETE}, {@link #INSERT}, or {@link #NEXT}
	 * @return
	 */
	public byte getOp() {
		return op;
	}
	
	/**
	 * Return the run-length of this operation
	 * @return
	 */
	public int getRun() {
		return run;
	}
	
	/**
	 * Return the data for this operation.  Only {@link #INSERT} and {@link #DELETE} have data.
	 * Only {@link #INSERT} is guaranteed to have data; {@link #DELETE} may have {@code null}. 
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	@Override
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		serial.writeObject(out, Byte.class, op);
		serial.writeObject(out, Integer.class, run);
		serial.writeObject(out, byte[].class, data);
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		op = serial.readObject(in, Byte.class);
		run = serial.readObject(in, Integer.class);
		data = serial.readObject(in, byte[].class);
	}
}
