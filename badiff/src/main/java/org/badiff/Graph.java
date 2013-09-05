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
					flags[pos] = DiffOp.NEXT;
					lengths[pos] = (short) (1 + lengths[pos - xval.length - 1]);
				}
				short dlen = x > 0 ? lengths[pos-1] : Short.MAX_VALUE;
				short ilen = y > 0 ? lengths[pos - xval.length] : Short.MAX_VALUE;
				if(dlen <= ilen) {
					flags[pos] = DiffOp.DELETE;
					lengths[pos] = (short) (dlen + 1);
				} else {
					flags[pos] = DiffOp.INSERT;
					lengths[pos] = (short) (ilen + 1);
				}
				
			}
		}
	}
	
	/**
	 * Returns a {@link List} of the {@link DiffOp}s for this graph in <b>reverse order</b>
	 * @return
	 */
	public List<DiffOp> rlist() {
		List<DiffOp> ret = new ArrayList<DiffOp>();

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte op = DiffOp.STOP;
		int run = 0;
		
		int pos = xval.length * yval.length - 1;
		while(pos > 0) {
			byte fop = flags[pos];
			if(op != DiffOp.STOP && op != fop) {
				byte[] data = null;
				if(op == DiffOp.INSERT || op == DiffOp.DELETE) {
					byte[] rdata = buf.toByteArray();
					data = new byte[rdata.length];
					for(int i = 0; i < rdata.length; i++) {
						data[data.length - i - 1] = rdata[i];
					}
				}
				ret.add(new DiffOp(op, run, data));
				run = 0;
				buf.reset();
			}
			op = fop;
			run++;
			if(op == DiffOp.INSERT) {
				buf.write(yval[pos / xval.length]);
				pos -= xval.length;
			}
			if(op == DiffOp.DELETE) {
				buf.write(xval[pos % xval.length]);
				pos -= 1;
			}
			if(op == DiffOp.NEXT)
				pos -= xval.length + 1;
		}
		
		if(op != DiffOp.STOP) {
			byte[] data = null;
			if(op == DiffOp.INSERT || op == DiffOp.DELETE) {
				byte[] rdata = buf.toByteArray();
				data = new byte[rdata.length];
				for(int i = 0; i < rdata.length; i++) {
					data[data.length - i - 1] = rdata[i];
				}
			}
			ret.add(new DiffOp(op, run, data));
		}
		
		return ret;
	}
}
