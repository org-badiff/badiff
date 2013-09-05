package org.badiff;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.badiff.io.Serialization;
import org.badiff.io.Serialized;

public class DiffOp implements Applyable, Serialized {
	private static final long serialVersionUID = 0;
	
	public static final byte STOP = 0x0;
	public static final byte DELETE = 0x1;
	public static final byte INSERT = 0x2;
	public static final byte NEXT = 0x3;
	
	private byte op;
	private int run;
	private byte[] data;
	
	public DiffOp(byte op, int run, byte[] data) {
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

	public byte getOp() {
		return op;
	}
	
	public int getRun() {
		return run;
	}
	
	public byte[] getData() {
		return data;
	}

	@Override
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		serial.writeObject(out, op);
		serial.writeObject(out, run);
		serial.writeObject(out, data);
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		op = serial.readObject(in, Byte.class);
		run = serial.readObject(in, Integer.class);
		data = serial.readObject(in, byte[].class);
	}
}
