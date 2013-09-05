package org.badiff.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.badiff.io.RuntimeIOException;
import org.badiff.io.Serialization;

public class Serials {

	public static <T> byte[] serialize(Serialization serial, Class<T> type, T object) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
			serial.writeObject(b, type, object);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
		return b.toByteArray();
	}
	
	public static <T> T deserialize(Serialization serial, Class<T> type, byte[] buf) {
		ByteArrayInputStream b = new ByteArrayInputStream(buf);
		try {
			return serial.readObject(b, type);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
	}
	
	private Serials() {
	}

}
