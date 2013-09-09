package org.badiff.imp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.badiff.Op;
import org.badiff.Storeable;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.Serialization;

public class StreamStoreable implements Storeable {
	
	protected OutputStream out;
	protected Serialization serial;
	
	public StreamStoreable(OutputStream out) {
		this(out, DefaultSerialization.getInstance());
	}
	
	public StreamStoreable(OutputStream out, Serialization serial) {
		this.out = out;
		this.serial = serial;
	}

	@Override
	public void store(Iterator<Op> ops) throws IOException {
		while(ops.hasNext()) {
			Op e = ops.next();
			e.serialize(serial, out);
		}
		new Op(Op.STOP, 1, null).serialize(serial, out);
	}

}
