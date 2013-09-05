package org.badiff.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.badiff.DiffOp;
import org.badiff.PatchOp;
import org.badiff.imp.MemoryDiff;
import org.badiff.imp.MemoryPatch;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerialization implements Serialization {
	public static final String STRIP_DELETES = KryoSerialization.class.getName() + ".strip_deletes";
	
	private Kryo kryo;
	
	public KryoSerialization() {
		this(new Kryo());
		
		kryo.setRegistrationRequired(false);
		kryo.setReferences(false);
		kryo.setAutoReset(true);
		
		kryo.addDefaultSerializer(Serialized.class, SerializedSerializer.class);
		
		kryo.register(byte[].class);
		kryo.register(DiffOp.class, new DiffOpSerializer());
		kryo.register(PatchOp.class, new SerializedSerializer<PatchOp>());
		kryo.register(MemoryDiff.class, new SerializedSerializer<MemoryDiff>());
		kryo.register(MemoryPatch.class, new SerializedSerializer<MemoryPatch>());
	}
	
	public KryoSerialization(Kryo kryo) {
		this.kryo = kryo;
	}

	@Override
	public <T> void writeObject(OutputStream out, Class<T> type, T object) throws IOException {
		if(out instanceof Output)
			kryo.writeObjectOrNull((Output) out, object, type);
		else {
			Output output = new Output(out);
			try {
				kryo.writeObjectOrNull(output, object, type);
			} finally {
				output.flush();
			}
		}
	}

	@Override
	public <T> T readObject(InputStream in, Class<T> type) throws IOException {
		Input input;
		if(in instanceof Input)
			input = (Input) in;
		else
			input = new Input(in, 1); // act like unbuffered stream
		return readObject(input, type);
	}

	@SuppressWarnings("unchecked")
	public KryoSerialization stripDeletes(boolean strip) {
		if(strip)
			kryo.getContext().put(KryoSerialization.STRIP_DELETES, true);
		else
			kryo.getContext().remove(KryoSerialization.STRIP_DELETES);
		return this;
	}

	@SuppressWarnings("unchecked")
	public boolean stripDeletes() {
		return kryo.getContext().containsKey(KryoSerialization.STRIP_DELETES);
	}

}
