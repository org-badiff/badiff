package org.badiff.kryo;

import java.io.IOException;

import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialized;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializedSerializer<T extends Serialized> extends Serializer<T> {

	@Override
	public void write(Kryo kryo, Output output, T object) {
		kryo.writeClass(output, object.getClass());
		try {
			object.serialize(new KryoSerialization(kryo), output);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}

	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
		Class<?> cls = kryo.readClass(input).getType();
		T object = type.cast(kryo.newInstance(cls));
		try {
			object.deserialize(new KryoSerialization(kryo), input);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
		return object;
	}

}
