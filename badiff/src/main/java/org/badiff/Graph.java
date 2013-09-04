package org.badiff;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Graph {
	protected byte[] flags;
	protected short[] lengths;
	protected byte[] xval;
	protected byte[] yval;
	
	public Graph(int size) {
		flags = new byte[size];
		lengths = new short[size];
		lengths[0] = Short.MIN_VALUE;
	}
	
	public void compute(byte[] orig, byte[] target) {
		xval = new byte[orig.length + 1];
		yval = new byte[target.length + 1];
		
		System.arraycopy(orig, 0, xval, 1, orig.length);
		System.arraycopy(target, 0, yval, 1, target.length);
		
		for(int y = 0; y < yval.length; y++) {
			for(int x = 0; x < xval.length; x++) {
				int pos = x + y * xval.length;
				if(x > 0 && y > 0 && xval[x] == yval[y]) {
					flags[pos] = Op.NEXT;
					lengths[pos] = (short) (1 + lengths[pos - xval.length - 1]);
				}
				short dlen = x > 0 ? lengths[pos-1] : Short.MAX_VALUE;
				short ilen = y > 0 ? lengths[pos - xval.length] : Short.MAX_VALUE;
				if(dlen <= ilen) {
					flags[pos] = Op.DELETE;
					lengths[pos] = (short) (dlen + 1);
				} else {
					flags[pos] = Op.INSERT;
					lengths[pos] = (short) (ilen + 1);
				}
				
			}
		}
	}
	
	/**
	 * Returns a {@link List} of the {@link Op}s for this graph in <b>reverse order</b>
	 * @return
	 */
	public List<Op> rlist() {
		List<Op> ret = new ArrayList<Op>();

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte op = Op.STOP;
		int run = 0;
		
		int pos = xval.length * yval.length - 1;
		while(pos > 0) {
			byte fop = flags[pos];
			if(op != Op.STOP && op != fop) {
				byte[] data = null;
				if(op == Op.INSERT || op == Op.DELETE)
					data = buf.toByteArray();
				ret.add(new Op(op, run, data));
				run = 0;
				buf.reset();
			}
			op = fop;
			run++;
			if(op == Op.INSERT)
				buf.write(yval[pos / xval.length]);
			if(op == Op.DELETE)
				buf.write(xval[pos % xval.length]);
		}
		
		if(op != Op.STOP) {
			byte[] data = null;
			if(op == Op.INSERT || op == Op.DELETE)
				data = buf.toByteArray();
			ret.add(new Op(op, run, data));
		}
		
		return ret;
	}
}
