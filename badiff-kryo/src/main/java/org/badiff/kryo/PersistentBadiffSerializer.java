package org.badiff.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.ObjectMap;

public class PersistentBadiffSerializer<T> extends BadiffSerializer<T> {

	public PersistentBadiffSerializer(Class<T> type, Kryo bytesKryo) {
		super(type, bytesKryo);
	}

	public PersistentBadiffSerializer(Class<T> type) {
		super(type);
	}

	@Override
	protected ObjectMap context(Kryo kryo) {
		return kryo.getContext();
	}
	
}
