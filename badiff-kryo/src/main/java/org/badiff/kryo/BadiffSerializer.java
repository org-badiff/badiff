package org.badiff.kryo;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import org.badiff.ByteArrayDiffs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.factories.ReflectionSerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

@SuppressWarnings("unchecked")
public class BadiffSerializer<T> extends Serializer<T> {
	protected Kryo bytesKryo;
	protected Class<T> bytesType;
	protected Serializer<T> bytesSerializer;
	
	public BadiffSerializer(Kryo bytesKryo, Class<T> bytesType) {
		this(bytesKryo, bytesType, null);
	}
	
	public BadiffSerializer(Kryo bytesKryo, Class<T> bytesType, Serializer<T> bytesSerializer) {
		this.bytesKryo = bytesKryo;
		this.bytesType = bytesType;
		this.bytesSerializer = bytesSerializer;
	}
	
	protected Serializer<T> getBytesSerializer() {
		if(bytesSerializer == null) {
			bytesSerializer = ReflectionSerializerFactory.makeSerializer(
					bytesKryo,
					FieldSerializer.class,
					bytesType);
		}
		return bytesSerializer;
	}
	
	protected byte[] toBytes(Kryo streamKryo, T object) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Output bytesOutput = new Output(bytes);
		bytesOutput.write(object == null ? 0 : 1);
		if(object != null) {
			if(bytesKryo == streamKryo)
				getBytesSerializer().write(bytesKryo, bytesOutput, object);
			else
				bytesKryo.writeObject(bytesOutput, object);
		}
		bytesOutput.close();
		return bytes.toByteArray();
	}
	
	protected T fromBytes(Kryo streamKryo, byte[] buf, Class<T> bytesType) {
		Input bytesInput = new Input(buf);
		if(bytesInput.read() == 0)
			return null;
		if(bytesKryo == streamKryo)
			return getBytesSerializer().read(bytesKryo, bytesInput, bytesType);
		else
			return bytesKryo.readObject(bytesInput, bytesType);
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
		byte[] next = setPreviousBytes(kryo, toBytes(kryo, object));
		byte[] diff = ByteArrayDiffs.udiff(previous, next);
		
		output.writeInt(diff.length, true);
		output.write(diff);
	}

	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
		byte[] diff = input.readBytes(input.readInt(true));
		byte[] previous = getPreviousBytes(kryo);
		byte[] next = setPreviousBytes(kryo, ByteArrayDiffs.apply(previous, diff));
		
		T object = fromBytes(kryo, next, type);
		return object;
	}

}
