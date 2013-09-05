package org.badiff.kryo;

import org.badiff.DiffOp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class DiffOpSerializer extends Serializer<DiffOp> {

	@Override
	public void write(Kryo kryo, Output output, DiffOp object) {
		output.writeByte(object.getOp());
		output.writeInt(object.getRun(), true);
		if(object.getOp() == DiffOp.INSERT)
			kryo.writeObject(output, object.getData());
		if(object.getOp() == DiffOp.DELETE) {
			byte[] data = object.getData();
			@SuppressWarnings("unchecked")
			boolean strip = kryo.getContext().containsKey(KryoSerialization.STRIP_DELETES);
			if(strip)
				data = null;
			kryo.writeObjectOrNull(output, data, byte[].class);
		}
	}

	@Override
	public DiffOp read(Kryo kryo, Input input, Class<DiffOp> type) {
		byte op = input.readByte();
		int run = input.readInt(true);
		byte[] data = null;
		if(op == DiffOp.INSERT)
			data = kryo.readObject(input, byte[].class);
		if(op == DiffOp.DELETE) {
			data = kryo.readObjectOrNull(input, byte[].class);
		}
		return new DiffOp(op, run, data);
	}

}
