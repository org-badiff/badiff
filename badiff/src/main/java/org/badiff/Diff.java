package org.badiff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.badiff.io.RuntimeIOException;
import org.badiff.q.ChukingOpQueue;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.ReplaceOpQueue;

public class Diff {
	
	public static OpQueue queue(byte[] orig, byte[] target) {
		return new ReplaceOpQueue(orig, target);
	}
	
	public static OpQueue improved(OpQueue q, int chunk) {
		q = new ChukingOpQueue(q, chunk);
		q = new GraphOpQueue(q, chunk);
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
