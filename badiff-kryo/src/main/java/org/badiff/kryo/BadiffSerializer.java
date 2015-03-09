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
		
		bytesKryo.setReferenceResolver(new UnwriteReferenceResolver(kryo.getReferenceResolver(), object));
		
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
		
		bytesKryo.setReferenceResolver(new UnreadReferenceResolver(kryo.getReferenceResolver(), type));
		
		byte[] diff = input.readBytes(input.readInt(true));
		byte[] previous = (byte[]) kryo.getGraphContext().get(this, new byte[0]);
		byte[] next = ByteArrayDiffs.apply(previous, diff);
		
		T object = fromBytes(next);
		
		kryo.getGraphContext().put(this, next);
		
		return object;
	}

	protected static class UnwriteReferenceResolver implements ReferenceResolver {
		protected ReferenceResolver parent;
		protected ReferenceResolver child;
		protected Object unwrite;
		protected int offset;
		
		public UnwriteReferenceResolver(ReferenceResolver parent, Object unwrite) {
			this.parent = parent;
			this.unwrite = unwrite;
			offset = parent.getWrittenId(unwrite) + 1;
			child = new MapReferenceResolver();
		}

		@Override
		public void setKryo(Kryo kryo) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getWrittenId(Object object) {
			if(object == unwrite) {
				int id = child.getWrittenId(object);
				if(id == -1)
					return id;
				return offset + id;
			}
			int id = parent.getWrittenId(object);
			if(id != -1)
				return id;
			id = child.getWrittenId(object);
			if(id != -1)
				return offset + id;
			return -1;
		}

		@Override
		public int addWrittenObject(Object object) {
			return offset + child.addWrittenObject(object);
		}

		@Override
		public int nextReadId(Class type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setReadObject(int id, Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getReadObject(Class type, int id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() {
			child.reset();
		}

		@Override
		public boolean useReferences(Class type) {
			return parent.useReferences(type);
		}
	}
	
	protected static class UnreadReferenceResolver implements ReferenceResolver {
		protected ReferenceResolver parent;
		protected ReferenceResolver child;
		protected int offset;
		
		public UnreadReferenceResolver(ReferenceResolver parent, Class<?> type) {
			this.parent = parent;
			offset = parent.nextReadId(type) - 1;
			child = new MapReferenceResolver();
		}

		@Override
		public void setKryo(Kryo kryo) {
		}

		@Override
		public int getWrittenId(Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int addWrittenObject(Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int nextReadId(Class type) {
			return offset + child.nextReadId(type);
		}

		@Override
		public void setReadObject(int id, Object object) {
			if(id < offset)
				parent.setReadObject(id, object);
			else
				child.setReadObject(id - offset, object);
		}

		@Override
		public Object getReadObject(Class type, int id) {
			if(id < offset)
				return parent.getReadObject(type, id);
			return child.getReadObject(type, id - offset);
		}

		@Override
		public void reset() {
			child.reset();
		}

		@Override
		public boolean useReferences(Class type) {
			return parent.useReferences(type);
		}

	}
}
