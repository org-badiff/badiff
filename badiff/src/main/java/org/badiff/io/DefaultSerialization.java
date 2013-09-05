package org.badiff.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.badiff.Op;
import org.badiff.imp.FileDiff;
import org.badiff.imp.MemoryDiff;
import org.badiff.patch.MemoryPatch;
import org.badiff.patch.PatchOp;
import org.badiff.util.Streams;

@SuppressWarnings({ "rawtypes", "unchecked" })
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

	private List<Serializer<?>> serializers = new ArrayList<Serializer<?>>();
	
	private DefaultSerialization() {
		serializers.add(new Serializer<Class>(Class.class) {

			@Override
			public void write(DataOutput out, Class obj) throws IOException {
				for(int i = 0; i < serializers.size(); i++) {
					if(serializers.get(i).type() == obj) {
						writeLong(out, i);
						return;
					}
				}
				throw new NotSerializableException(obj.getName());
			}

			@Override
			public Class read(DataInput in) throws IOException {
				return serializers.get((int) readLong(in)).type();
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
				writeLong(out, obj);
			}

			@Override
			public Integer read(DataInput in) throws IOException {
				return (int) readLong(in);
			}
		});
		serializers.add(new Serializer<Long>(Long.class) {

			@Override
			public void write(DataOutput out, Long obj) throws IOException {
				writeLong(out, obj);
			}

			@Override
			public Long read(DataInput in) throws IOException {
				return readLong(in);
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
				writeLong(out, obj != null ? obj.length + 1 : 0);
				if(obj != null)
					out.write(obj);
			}

			@Override
			public byte[] read(DataInput in) throws IOException {
				int size = (int) readLong(in) - 1;
				if(size == -1)
					return null;
				byte[] obj = new byte[size];
				in.readFully(obj);
				return obj;
			}
		});
		
		serializers.add(new Serializer<Op>(Op.class) {

			@Override
			public void write(DataOutput out, Op obj) throws IOException {
				obj.serialize(getInstance(), Streams.asStream(out));
			}

			@Override
			public Op read(DataInput in) throws IOException {
				Op op = new Op();
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
		
		serializers.add(new Serializer<FileDiff>(FileDiff.class) {

			@Override
			public void write(DataOutput out, FileDiff obj)
					throws IOException {
				obj.serialize(getInstance(), Streams.asStream(out));
			}

			@Override
			public FileDiff read(DataInput in) throws IOException {
				FileDiff md = new FileDiff(File.createTempFile("FileDiff", ".tmp"));
				md.deserialize(getInstance(), Streams.asStream(in));
				return md;
			}
		});
		
		serializers.add(new Serializer<MemoryPatch>(MemoryPatch.class) {

			@Override
			public void write(DataOutput out, MemoryPatch obj)
					throws IOException {
				obj.serialize(getInstance(), Streams.asStream(out));
			}

			@Override
			public MemoryPatch read(DataInput in) throws IOException {
				MemoryPatch mp = new MemoryPatch();
				mp.deserialize(getInstance(), Streams.asStream(in));
				return mp;
			}
		});

		serializers.add(new Serializer<PatchOp>(PatchOp.class) {

			@Override
			public void write(DataOutput out, PatchOp obj)
					throws IOException {
				obj.serialize(getInstance(), Streams.asStream(out));
			}

			@Override
			public PatchOp read(DataInput in) throws IOException {
				PatchOp op = new PatchOp();
				op.deserialize(getInstance(), Streams.asStream(in));
				return op;
			}
		});
	}
	
	public void writeLong(DataOutput out, long val) throws IOException {
		int v = (int)(val & 0x7f);
		out.writeByte(v);
		if(val != v)
			writeLong(out, val >>> 7);
	}
	
	public long readLong(DataInput in) throws IOException {
		return readLong(in, 0, 0);
	}
	
	private long readLong(DataInput in, long accum, int shift) throws IOException {
		int b = 0xff & in.readByte();
		accum |= (b & 0x7f) << (shift);
		if((b & 0x80) == 0)
			return accum;
		return readLong(in, accum, shift + 7);
	}
	
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
