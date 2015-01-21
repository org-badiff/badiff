package org.badiff.kryo;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import org.badiff.ByteArrayDiffs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

@SuppressWarnings("unchecked")
public class BadiffSerializer<T> extends Serializer<T> {
	protected Kryo bytesKryo;
	protected Serializer<T> bytesSerializer;
	
	public BadiffSerializer(Kryo bytesKryo, Class<T> type) {
		this(bytesKryo, bytesKryo.getDefaultSerializer(type));
	}
	
	public BadiffSerializer(Kryo bytesKryo, Serializer<T> bytesSerializer) {
		this.bytesKryo = bytesKryo;
		this.bytesSerializer = bytesSerializer;
	}
	
	protected byte[] toBytes(T object) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Output bytesOutput = new Output(bytes);
		bytesKryo.writeClass(bytesOutput, object == null ? null : object.getClass());
		if(object != null)
			bytesKryo.writeObject(bytesOutput, object, bytesSerializer);
		bytesOutput.close();
		return bytes.toByteArray();
	}
	
	protected T fromBytes(byte[] buf) {
		Input bytesInput = new Input(buf);
		Class<? extends T> bytesType = bytesKryo.readClass(bytesInput).getType();
		if(bytesType == null)
			return null;
		return bytesKryo.readObject(bytesInput, bytesType, bytesSerializer);
	}
	
	protected byte[] getPreviousBytes(Kryo kryo) {
		byte[] previous = (byte[]) kryo.getGraphContext().get(this);
		if(previous == null)
			setPreviousBytes(kryo, previous = new byte[0]);
		return previous;
	}
	
	protected byte[] setPreviousBytes(Kryo kryo, byte[] next) {
		kryo.getGraphContext().put(this, next);
		return next;
	}
	
	@Override
	public void write(Kryo kryo, Output output, T object) {
		byte[] previous = getPreviousBytes(kryo);
		byte[] next = setPreviousBytes(kryo, toBytes(object));
		byte[] diff = ByteArrayDiffs.udiff(previous, next);
		
		output.writeInt(diff.length, true);
		output.write(diff);
	}

	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
		byte[] diff = input.readBytes(input.readInt(true));
		byte[] previous = getPreviousBytes(kryo);
		byte[] next = setPreviousBytes(kryo, ByteArrayDiffs.apply(previous, diff));
		
		T object = fromBytes(next);
		kryo.reference(object);
		return object;
	}

}
