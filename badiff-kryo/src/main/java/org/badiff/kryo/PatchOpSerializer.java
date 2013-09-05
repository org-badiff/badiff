package org.badiff.kryo;

import org.badiff.Diff;
import org.badiff.PatchOp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PatchOpSerializer extends Serializer<PatchOp> {

	@Override
	public void write(Kryo kryo, Output output, PatchOp object) {
		output.writeByte(object.getOp());
		kryo.writeClassAndObject(output, object.getDiff());
	}

	@Override
	public PatchOp read(Kryo kryo, Input input, Class<PatchOp> type) {
		byte op = input.readByte();
		Diff diff = (Diff) kryo.readClassAndObject(input);
		return new PatchOp(op, diff);
	}
	
}
