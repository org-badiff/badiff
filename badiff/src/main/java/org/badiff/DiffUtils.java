package org.badiff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
	
	public static OpQueue diff(byte[] orig, byte[] target) {
		return new ReplaceOpQueue(orig, target);
	}
	
	public static OpQueue diff(InputStream orig, InputStream target) {
		return new StreamChunkingOpQueue(orig, target);
	}
	
	public static OpQueue improved(OpQueue q) {
		q = new ChukingOpQueue(q);
		q = new ParallelGraphOpQueue(q);
		q = new CoalescingOpQueue(q);
		return q;
	}
	
	public static byte[] applyDiff(Diff diff, byte[] orig) {
		ByteArrayInputStream in = new ByteArrayInputStream(orig);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			diff.applyDiff(in, out);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
		return out.toByteArray();
	}

	private DiffUtils() {
	}

}
