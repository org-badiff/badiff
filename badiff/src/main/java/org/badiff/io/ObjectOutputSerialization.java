package org.badiff.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

public class ObjectOutputSerialization implements Serialization {
	
	protected Map<Object, Object> context;
	protected GraphContext graphContext;
	protected int depth;
	
	public ObjectOutputSerialization() {
		context = new HashMap<Object, Object>();
		graphContext = new GraphContext(context);
		depth = 0;
	}

	@Override
	public <T> void writeObject(DataOutput out, Class<T> type, T object)
			throws IOException {
		ObjectOutput oo = (ObjectOutput) out;
		if(depth == 0)
			graphContext.clear();
	}

	@Override
	public <T> T readObject(DataInput in, Class<T> type) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<Object, Object> context() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphContext graphContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
