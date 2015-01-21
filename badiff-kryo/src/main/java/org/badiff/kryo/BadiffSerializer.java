package org.badiff.kryo;

import java.io.ByteArrayOutputStream;
import org.badiff.ByteArrayDiffs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.factories.ReflectionSerializerFactory;
import com.esotericsoftware.kryo.factories.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.ObjectMap;

@SuppressWarnings("unchecked")
public class BadiffSerializer<T> extends Serializer<T> {
	protected Kryo bytesKryo;
	protected ObjectMap<Class<?>, Serializer<?>> serializers = new ObjectMap<Class<?>, Serializer<?>>(8);
	protected SerializerFactory factory;
	
	public BadiffSerializer(Kryo bytesKryo) {
		this(bytesKryo, null);
	}
	
	public BadiffSerializer(Kryo bytesKryo, SerializerFactory factory) {
		this.bytesKryo = bytesKryo;
		this.factory = factory != null ? factory : createSerializerFactory();
	}
	
	public BadiffSerializer(Kryo bytesKryo, Class<T> bytesType, Serializer<T> bytesSerializer) {
		this.bytesKryo = bytesKryo;
		
		serializers.put(bytesType, bytesSerializer);
	}
	
	protected Class<? extends Serializer> getDefaultSerializerClass() {
		return CompatibleFieldSerializer.class;
	}
	
	protected SerializerFactory createSerializerFactory() {
		return new SerializerFactory() {
			@Override
			public Serializer makeSerializer(Kryo kryo, Class<?> type) {
				Serializer s = kryo.getSerializer(type);
				if(s == BadiffSerializer.this)
					s = ReflectionSerializerFactory.makeSerializer(
							kryo,
							getDefaultSerializerClass(),
							type);
				return s;
			}
		};
	}
	
	protected Serializer<T> getBytesSerializer(Class<?> streamType) {
		Serializer<T> bytesSerializer = (Serializer<T>) serializers.get(streamType);
		if(bytesSerializer == null) {
			if(factory == null)
				throw new IllegalStateException(this + " is noy configured for " + streamType);
			bytesSerializer = factory.makeSerializer(bytesKryo, streamType);
			serializers.put(streamType, bytesSerializer);
		}
		return bytesSerializer;
	}
	
	protected byte[] toBytes(Kryo streamKryo, T object) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Output bytesOutput = new Output(bytes);
		bytesOutput.write(object == null ? 0 : 1);
		if(object != null) {
			if(bytesKryo == streamKryo)
				getBytesSerializer(object.getClass()).write(bytesKryo, bytesOutput, object);
			else
				bytesKryo.writeObject(bytesOutput, object);
		}
		bytesOutput.close();
		return bytes.toByteArray();
	}
	
	protected T fromBytes(Kryo streamKryo, byte[] buf, Class<T> streamType) {
		Input bytesInput = new Input(buf);
		if(bytesInput.read() == 0)
			return null;
		if(bytesKryo == streamKryo)
			return getBytesSerializer(streamType).read(bytesKryo, bytesInput, streamType);
		else
			return bytesKryo.readObject(bytesInput, streamType);
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
