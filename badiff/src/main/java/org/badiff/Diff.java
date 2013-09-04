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

public class Diff {
	
	public static OpQueue diff(byte[] orig, byte[] target) {
		return new ReplaceOpQueue(orig, target);
	}
	
	public static OpQueue diff(InputStream orig, InputStream target, int chunk) {
		return new StreamChunkingOpQueue(orig, target, chunk);
	}
	
	public static OpQueue improved(OpQueue q, int chunk) {
		q = new ChukingOpQueue(q, chunk);
		q = new ParallelGraphOpQueue(q);
		q = new CoalescingOpQueue(q);
		return q;
	}
	
	public static byte[] applyDiff(BADiff diff, byte[] orig) {
		ByteArrayInputStream in = new ByteArrayInputStream(orig);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			diff.applyDiff(in, out);
		} catch(IOException ioe) {
			throw new RuntimeIOException(ioe);
		}
		return out.toByteArray();
	}

	private Diff() {
	}

}
