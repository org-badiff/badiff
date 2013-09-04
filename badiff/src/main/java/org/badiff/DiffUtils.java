package org.badiff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.badiff.io.RuntimeIOException;
import org.badiff.q.ChukingOpQueue;
import org.badiff.q.CoalescingOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.ParallelGraphOpQueue;
import org.badiff.q.ReplaceOpQueue;
import org.badiff.q.StreamChunkingOpQueue;

public class DiffUtils {
	
	public static OpQueue queue(byte[] orig, byte[] target) {
		return new ReplaceOpQueue(orig, target);
	}
	
	public static OpQueue queue(InputStream orig, InputStream target) {
		return new StreamChunkingOpQueue(orig, target);
	}
	
	public static OpQueue improved(OpQueue q) {
		q = new ChukingOpQueue(q);
		q = new ParallelGraphOpQueue(q);
		q = new CoalescingOpQueue(q);
		return q;
	}
	
	public static byte[] apply(Applyable a, byte[] orig) {
		ByteArrayInputStream in = new ByteArrayInputStream(orig);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			a.apply(in, out);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
		return out.toByteArray();
	}
	
	public static void apply(Applyable a, File orig) throws IOException {
		File tmp = File.createTempFile(orig.getName(), ".tmp");
		FileInputStream in = new FileInputStream(orig);
		FileOutputStream out = new FileOutputStream(tmp);
		a.apply(in, out);
		out.close();
		in.close();
		if(!orig.delete() || !tmp.renameTo(orig))
			throw new IOException("Unable to move " + tmp + " to " + orig);
	}

	private DiffUtils() {
	}

}
