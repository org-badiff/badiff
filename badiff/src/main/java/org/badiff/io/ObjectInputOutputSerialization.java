package org.badiff.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.badiff.util.Types;

public class ObjectInputOutputSerialization implements Serialization {
	
	protected Map<Object, Object> context;
	protected GraphContext graphContext;
	protected int depth;
	
	public ObjectInputOutputSerialization() {
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
		depth++;
		try {
			if(Types.isPrimitive(type)) {
				if(type == boolean.class) oo.writeBoolean((Boolean) object);
				else if(type == byte.class) oo.writeByte((Byte) object);
				else if(type == char.class) oo.writeChar((Character) object);
				else if(type == double.class) oo.writeDouble((Double) object);
				else if(type == float.class) oo.writeFloat((Float) object);
				else if(type == int.class) oo.writeInt((Integer) object);
				else if(type == long.class) oo.writeLong((Long) object);
				else if(type == short.class) oo.writeShort((Short) object);
			} else if(!type.isInstance(object))
				throw new IllegalArgumentException("Object does not have specified type (" + type + "): " + object);
			else if(!Serialized.class.isAssignableFrom(type))
				throw new IllegalArgumentException("Specified type (" + type + ") does not inherit from " + Serialized.class);
			else
				((Serialized) object).serialize(this, oo);
		} finally {
			depth--;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readObject(DataInput in, Class<T> type) throws IOException {
		ObjectInput oi = (ObjectInput) in;
		if(depth == 0)
			graphContext.clear();
		depth++;
		try {
			if(Types.isPrimitive(type)) {
				if(type == boolean.class) return (T) (Boolean) oi.readBoolean();
				else if(type == byte.class) return (T) (Byte) oi.readByte();
				else if(type == char.class) return (T) (Character) oi.readChar();
				else if(type == double.class) return (T) (Double) oi.readDouble();
				else if(type == float.class) return (T) (Float) oi.readFloat();
				else if(type == int.class) return (T) (Integer) oi.readInt();
				else if(type == long.class) return (T) (Long) oi.readLong();
				else if(type == short.class) return (T) (Short) oi.readShort();
				else throw new IllegalStateException(type + " supposedly primitive but not");
			} else if(!Serialized.class.isAssignableFrom(type))
				throw new IllegalArgumentException("Specified type (" + type + ") does not inherit from " + Serialized.class);
			else {
				T object;
				try {
					object = type.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("Unable to instantiate " + type, e);
				}
				((Serialized) object).deserialize(this, oi);
				return object;
			}
		} finally {
			depth--;
		}
	}

	@Override
	public Map<Object, Object> context() {
		return context;
	}

	@Override
	public GraphContext graphContext() {
		return graphContext;
	}

}
