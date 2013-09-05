package org.badiff.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.badiff.DiffOp;
import org.badiff.imp.MemoryDiff;
import org.badiff.util.Streams;

public class DefaultSerialization implements Serialization {
	
	private static DefaultSerialization instance = new DefaultSerialization();
	public static DefaultSerialization getInstance() {
		return instance;
	}

	private static abstract class Serializer<T> {
		protected Class<T> type;
		
		public Serializer(Class<T> type) {
			this.type = type;
		}
		
		public Class<T> type() {
			return type;
		}
		public abstract void write(DataOutput out, T obj) throws IOException;
		public abstract T read(DataInput in) throws IOException;
	}

	private static List<Serializer<?>> serializers = new ArrayList<Serializer<?>>();
	static {
		serializers.add(new Serializer<Class>(Class.class) {

			@Override
			public void write(DataOutput out, Class obj) throws IOException {
				for(int i = 0; i < serializers.size(); i++) {
					if(serializers.get(i).type() == obj) {
						out.writeInt(i);
						return;
					}
				}
				throw new NotSerializableException(obj.getName());
			}

			@Override
			public Class read(DataInput in) throws IOException {
				return serializers.get(in.readInt()).type();
			}
		});
		serializers.add(new Serializer<Byte>(Byte.class) {

			@Override
			public void write(DataOutput out, Byte obj) throws IOException {
				out.writeByte(obj);
			}

			@Override
			public Byte read(DataInput in) throws IOException {
				return in.readByte();
			}
		});
		serializers.add(new Serializer<Integer>(Integer.class) {

			@Override
			public void write(DataOutput out, Integer obj) throws IOException {
				out.writeInt(obj);
			}

			@Override
			public Integer read(DataInput in) throws IOException {
				return in.readInt();
			}
		});
		serializers.add(new Serializer<Long>(Long.class) {

			@Override
			public void write(DataOutput out, Long obj) throws IOException {
				out.writeLong(obj);
			}

			@Override
			public Long read(DataInput in) throws IOException {
				return in.readLong();
			}
		});
		serializers.add(new Serializer<String>(String.class) {

			@Override
			public void write(DataOutput out, String obj) throws IOException {
				out.writeUTF(obj);
			}

			@Override
			public String read(DataInput in) throws IOException {
				return in.readUTF();
			}
		});
		serializers.add(new Serializer<byte[]>(byte[].class) {

			@Override
			public void write(DataOutput out, byte[] obj) throws IOException {
				out.writeInt(obj.length);
				out.write(obj);
			}

			@Override
			public byte[] read(DataInput in) throws IOException {
				byte[] obj = new byte[in.readInt()];
				in.readFully(obj);
				return obj;
			}
		});
		
		serializers.add(new Serializer<DiffOp>(DiffOp.class) {

			@Override
			public void write(DataOutput out, DiffOp obj) throws IOException {
				obj.serialize(getInstance(), Streams.asStream(out));
			}

			@Override
			public DiffOp read(DataInput in) throws IOException {
				DiffOp op = new DiffOp();
				op.deserialize(getInstance(), Streams.asStream(in));
				return op;
				 
			}
		});
		
		serializers.add(new Serializer<MemoryDiff>(MemoryDiff.class) {

			@Override
			public void write(DataOutput out, MemoryDiff obj)
					throws IOException {
				obj.serialize(getInstance(), Streams.asStream(out));
			}

			@Override
			public MemoryDiff read(DataInput in) throws IOException {
				MemoryDiff md = new MemoryDiff();
				md.deserialize(getInstance(), Streams.asStream(in));
				return md;
			}
		});
	}
	
	private DefaultSerialization() {}
	
	@Override
	public <T> void writeObject(OutputStream out, Class<T> type, T object)
			throws IOException {
		for(Serializer s : serializers) {
			if(s.type() == type) {
				s.write(new DataOutputStream(out), object);
				return;
			}
		}
		throw new NotSerializableException(type.getName());
	}
	@Override
	public <T> T readObject(InputStream in, Class<T> type) throws IOException {
		for(Serializer s : serializers) {
			if(s.type() == type) {
				return (T) s.read(new DataInputStream(in));
			}
		}
		throw new NotSerializableException(type.getName());
	}
	
}
