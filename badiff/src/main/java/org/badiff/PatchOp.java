package org.badiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.badiff.io.EmptyInputStream;
import org.badiff.util.Streams;

public class PatchOp implements FileApplyable, Serializable {
	private static final long serialVersionUID = 0;
	
	public static final byte SKIP = 0x0; // no diff
	public static final byte DELETE = 0x1; // no diff
	public static final byte CREATE = 0x2; // has diff
	public static final byte DIFF = 0x3; // has diff
	
	private byte op;
	private Diff diff;
	
	public PatchOp(byte op, Diff diff) {
		if((op & 0x3) != op)
			throw new IllegalArgumentException("invalid op");
		if((op == CREATE || op == DIFF) && diff == null)
			throw new IllegalArgumentException("diff required");
		this.op = op;
		this.diff = diff;
	}
	
	public byte getOp() {
		return op;
	}
	
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
}
