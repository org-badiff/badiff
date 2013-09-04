package org.badiff.kryo;

import org.badiff.Op;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class OpSerializer extends Serializer<Op> {

	@Override
	public void write(Kryo kryo, Output output, Op object) {
		output.writeByte(object.getOp());
		output.writeInt(object.getRun(), true);
		if(object.getOp() == Op.INSERT)
			kryo.writeObject(output, object.getData());
	}

	@Override
	public Op read(Kryo kryo, Input input, Class<Op> type) {
		byte op = input.readByte();
		int run = input.readInt(true);
		byte[] data = null;
		if(op == Op.INSERT)
			data = kryo.readObject(input, byte[].class);
		return new Op(op, run, data);
	}

}
