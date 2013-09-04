package org.badiff.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.badiff.Op;
import org.badiff.io.Serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerialization extends Kryo implements Serialization {
	
	public KryoSerialization() {
		setRegistrationRequired(true);
		setReferences(false);
		setAutoReset(true);
		
		register(byte[].class);
		
		register(Op.class, new OpSerializer());
	}

	@Override
	public void writeObject(OutputStream out, Object object) throws IOException {
		Output output = new Output(out);
		try {
			writeObject(output, object);
		} finally {
			output.flush();
		}
	}

	@Override
	public <T> T readObject(InputStream in, Class<T> type) throws IOException {
		Input input = new Input(in, 1); // act like unbuffered stream
		return readObject(input, type);
	}

}
