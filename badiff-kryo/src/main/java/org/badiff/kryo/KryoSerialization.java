package org.badiff.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.badiff.DiffOp;
import org.badiff.io.Serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerialization extends Kryo implements Serialization {
	public static final String STRIP_DELETES = KryoSerialization.class.getName() + ".strip_deletes";
	
	public KryoSerialization() {
		setRegistrationRequired(true);
		setReferences(false);
		setAutoReset(true);
		
		register(byte[].class);
		
		register(DiffOp.class, new DiffOpSerializer());
	}

	@Override
	public void writeObject(OutputStream out, Object object) throws IOException {
		if(out instanceof Output)
			writeObject((Output) out, object);
		else {
			Output output = new Output(out);
			try {
				writeObject(output, object);
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
			getContext().put(KryoSerialization.STRIP_DELETES, true);
		else
			getContext().remove(KryoSerialization.STRIP_DELETES);
		return this;
	}

	@SuppressWarnings("unchecked")
	public boolean stripDeletes() {
		return getContext().containsKey(KryoSerialization.STRIP_DELETES);
	}

}
