package org.badiff.patcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.badiff.io.DefaultSerialization;
import org.badiff.util.Streams;

public class PatcherSerialization extends DefaultSerialization {
	public static PatcherSerialization newInstance() {
		return new PatcherSerialization();
	}
	
	public PatcherSerialization() {
		addSerializer(new Serializer<PathDiff>(PathDiff.class) {
			@Override
			public void write(DataOutput out, PathDiff obj) throws IOException {
				obj.serialize(PatcherSerialization.this, Streams.asStream(out));
			}

			@Override
			public PathDiff read(DataInput in) throws IOException {
				PathDiff object = new PathDiff();
				object.deserialize(PatcherSerialization.this, Streams.asStream(in));
				return object;
			}
		});
		addSerializer(new Serializer<PathDigest>(PathDigest.class) {

			@Override
			public void write(DataOutput out, PathDigest obj)
					throws IOException {
				obj.serialize(PatcherSerialization.this, Streams.asStream(out));
			}

			@Override
			public PathDigest read(DataInput in) throws IOException {
				PathDigest object = new PathDigest();
				object.deserialize(PatcherSerialization.this, Streams.asStream(in));
				return object;
			}
		});
		addSerializer(new Serializer<SerializedDigest>(SerializedDigest.class) {

			@Override
			public void write(DataOutput out, SerializedDigest obj)
					throws IOException {
				obj.serialize(PatcherSerialization.this, Streams.asStream(out));
			}

			@Override
			public SerializedDigest read(DataInput in) throws IOException {
				SerializedDigest object = new SerializedDigest();
				object.deserialize(PatcherSerialization.this, Streams.asStream(in));
				return object;
			}
		});
	}
}
