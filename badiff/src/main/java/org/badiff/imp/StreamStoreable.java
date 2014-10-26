package org.badiff.imp;

import java.io.DataOutput;
import java.io.DataOutputStream;
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
	protected DataOutput data;
	
	public StreamStoreable(OutputStream out) {
		this(out, DefaultSerialization.newInstance());
	}
	
	public StreamStoreable(OutputStream out, Serialization serial) {
		this.out = out;
		this.serial = serial;
		data = new DataOutputStream(out);
	}

	@Override
	public void store(Iterator<Op> ops) throws IOException {
		while(ops.hasNext()) {
			Op e = ops.next();
			e.serialize(serial, data);
		}
		new Op(Op.STOP, 1, null).serialize(serial, data);
	}

}
