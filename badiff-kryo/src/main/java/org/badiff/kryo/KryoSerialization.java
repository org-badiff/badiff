package org.badiff.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.badiff.io.GraphContext;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerialization implements Serialization {
	
	private static class SerializedSerializer extends Serializer<Serialized> {
		@Override
		public void write(Kryo kryo, Output output, Serialized object) {
			@SuppressWarnings("unchecked")
			KryoSerialization serial = (KryoSerialization) kryo.getContext().get(KryoSerialization.class);
			try {
				object.serialize(serial, output);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Serialized read(Kryo kryo, Input input, Class<Serialized> type) {
			@SuppressWarnings("unchecked")
			KryoSerialization serial = (KryoSerialization) kryo.getContext().get(KryoSerialization.class);
			Serialized object = kryo.newInstance(type);
			kryo.reference(object);
			try {
				object.deserialize(serial, input);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			return object;
		}
		
	}
	
	private Kryo kryo;
	private GraphContext graphContext;
	
	public KryoSerialization() {
		this(new Kryo());
	}
	
	@SuppressWarnings("unchecked")
	public KryoSerialization(Kryo kryo) {
		this.kryo = kryo;
		this.kryo.addDefaultSerializer(Serialized.class, SerializedSerializer.class);
		this.kryo.getContext().put(KryoSerialization.class, this);
		graphContext = new GraphContext((Map<Object, Object>) this.kryo.getGraphContext());
	}

	private static Output wrap(OutputStream out) {
		if(out instanceof Output)
			return (Output) out;
		return new Output(out);
	}
	
	private static Input wrap(InputStream in) {
		if(in instanceof Input)
			return (Input) in;
		return new Input(in);
	}
	
	@Override
	public <T> void writeObject(OutputStream out, Class<T> type, T object) throws IOException {
		kryo.writeObjectOrNull(wrap(out), object, type);
	}

	@Override
	public <T> T readObject(InputStream in, Class<T> type) throws IOException {
		return kryo.readObjectOrNull(wrap(in), type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Object, Object> context() {
		return (Map<Object, Object>) kryo.getContext();
	}

	@Override
	public GraphContext graphContext() {
		return graphContext;
	}

}
