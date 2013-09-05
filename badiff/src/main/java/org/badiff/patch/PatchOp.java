package org.badiff.patch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.badiff.Diff;
import org.badiff.io.EmptyInputStream;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.util.Streams;

/**
 * An operation in a {@link Patch} that can be applied to a {@link File}
 * @author robin
 *
 */
public class PatchOp implements FileApplyable, Serialized {
	private static final long serialVersionUID = 0;
	
	/**
	 * Do nothing to the file
	 */
	public static final byte SKIP = 0x0; // no diff
	/**
	 * Delete the file
	 */
	public static final byte DELETE = 0x1; // no diff
	/**
	 * Create the file by applying a {@link Diff} to an empty {@link InputStream}
	 */
	public static final byte CREATE = 0x2; // has diff
	/**
	 * Apply a {@link Diff} to the file
	 */
	public static final byte DIFF = 0x3; // has diff
	
	/**
	 * The operation
	 */
	private byte op;
	/**
	 * The diff
	 */
	private Diff diff;
	
	/*
	 * Required for deserialization
	 */
	public PatchOp() {}
	
	/**
	 * Create a new {@link PatchOp}
	 * @param op
	 * @param diff
	 */
	public PatchOp(byte op, Diff diff) {
		if((op & 0x3) != op)
			throw new IllegalArgumentException("invalid op");
		if((op == CREATE || op == DIFF) && diff == null)
			throw new IllegalArgumentException("diff required");
		this.op = op;
		this.diff = diff;
	}
	
	/**
	 * Return the operation for this {@link PatchOp}, one of {@link #SKIP}, {@link #DELETE},
	 * {@link #CREATE}, or {@link #DIFF}
	 * @return
	 */
	public byte getOp() {
		return op;
	}
	
	/**
	 * Returns the diff for this {@link PatchOp}, or {@code null}.  Diffs are required
	 * for {@link #CREATE} and {@link #DIFF} operations.
	 * @return
	 */
	public Diff getDiff() {
		return diff;
	}

	@Override
	public void apply(File orig, File target) throws IOException {
		switch(op) {
		case SKIP:
			InputStream in = new FileInputStream(orig);
			OutputStream out = new FileOutputStream(target);
			Streams.copy(in, out);
			out.close();
			in.close();
			break;
		case DELETE:
			target.delete();
			break;
		case CREATE:
			in = new EmptyInputStream();
			out = new FileOutputStream(target);
			diff.apply(in, out);
			out.close();
			in.close();
			break;
		case DIFF:
			in = new FileInputStream(orig);
			out = new FileOutputStream(target);
			diff.apply(in, out);
			out.close();
			in.close();
			break;
		}
	}

	@Override
	public void apply(File orig) throws IOException {
		switch(op) {
		case SKIP:
			break;
		case DELETE:
			orig.delete();
			break;
		case CREATE:
			InputStream in = new EmptyInputStream();
			OutputStream out = new FileOutputStream(orig);
			diff.apply(in, out);
			out.close();
			in.close();
			break;
		case DIFF:
			File tmp = File.createTempFile(orig.getName(), ".tmp");
			in = new FileInputStream(orig);
			out = new FileOutputStream(tmp);
			diff.apply(in, out);
			out.close();
			in.close();
			if(!orig.delete() || !tmp.renameTo(orig))
				throw new IOException("Unable to replace " + orig);
			break;
		}
	}

	@Override
	public void serialize(Serialization serial, OutputStream out)
			throws IOException {
		serial.writeObject(out, Byte.class, op);
		serial.writeObject(out, Class.class, diff == null ? null : diff.getClass());
		if(diff != null)
			serial.writeObject(out, Diff.class, diff);
	}

	@Override
	public void deserialize(Serialization serial, InputStream in)
			throws IOException {
		op = serial.readObject(in, Byte.class);
		@SuppressWarnings("unchecked")
		Class<? extends Diff> type = serial.readObject(in, Class.class);
		if(type != null)
			diff = serial.readObject(in, type);
	}
}
