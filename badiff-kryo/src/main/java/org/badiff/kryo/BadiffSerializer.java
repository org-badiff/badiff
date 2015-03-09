package org.badiff.kryo;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.badiff.ByteArrayDiffs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ReferenceResolver;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.MapReferenceResolver;

public class BadiffSerializer<T> extends Serializer<T> {
	protected Kryo bytesKryo;
	protected Class<T> type;
	
	public BadiffSerializer(Class<T> type) {
		this(type, new Kryo());
	}
	
	public BadiffSerializer(Class<T> type, Kryo bytesKryo) {
		this.bytesKryo = bytesKryo;
		this.type = type;
	}
	
	protected byte[] toBytes(T object) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Output bytesOutput = new Output(bytes);
		bytesKryo.writeClassAndObject(bytesOutput, object);
		bytesOutput.close();
		return bytes.toByteArray();
	}
	
	protected T fromBytes(byte[] buf) {
		Input bytesInput = new Input(buf);
		return type.cast(bytesKryo.readClassAndObject(bytesInput));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void write(Kryo kryo, Output output, T object) {
		if(kryo == bytesKryo)
			throw new IllegalArgumentException("Cannot re-use Kryo instances");
		
		byte[] previous = (byte[]) kryo.getGraphContext().get(this, new byte[0]);
		byte[] next = toBytes(object);
		byte[] diff = ByteArrayDiffs.udiff(previous, next);
		
		output.writeInt(diff.length, true);
		output.write(diff);
		
		kryo.getGraphContext().put(this, next);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T read(Kryo kryo, Input input, Class<T> type) {
		if(kryo == bytesKryo)
			throw new IllegalArgumentException("Cannot re-use Kryo instances");
		
		byte[] diff = input.readBytes(input.readInt(true));
		byte[] previous = (byte[]) kryo.getGraphContext().get(this, new byte[0]);
		byte[] next = ByteArrayDiffs.apply(previous, diff);
		
		T object = fromBytes(next);
		
		kryo.getGraphContext().put(this, next);
		
		return object;
	}
}
