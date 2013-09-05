package org.badiff.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JdkSerialization implements Serialization {
	private static final JdkSerialization instance = new JdkSerialization();
	public static JdkSerialization getInstance() {
		return instance;
	}
	
	private JdkSerialization() {}

	@Override
	public <T> void writeObject(OutputStream out, Class<T> type, T object) throws IOException {
		type.cast(object);
		ObjectOutputStream oo = new ObjectOutputStream(new NonClosingOutputStream(out));
		oo.writeObject(object);
		oo.close();
	}

	@Override
	public <T> T readObject(InputStream in, Class<T> type) throws IOException {
		ObjectInputStream oi = new ObjectInputStream(new NonClosingInputStream(in));
		try {
			return type.cast(oi.readObject());
		} catch(ClassNotFoundException cnfe) {
			throw new IOException(cnfe);
		} finally {
			oi.close();
		}
	}

}
