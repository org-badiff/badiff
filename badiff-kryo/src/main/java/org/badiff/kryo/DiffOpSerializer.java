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
			@SuppressWarnings("unchecked")
			boolean strip = object.getData() == null || kryo.getContext().containsKey(KryoSerialization.STRIP_DELETES);
			output.writeBoolean(strip);
			if(!strip)
				kryo.writeObject(output, object.getData());
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
			boolean stripped = input.readBoolean();
			if(!stripped)
				data = kryo.readObject(input, byte[].class);
		}
		return new DiffOp(op, run, data);
	}

}
