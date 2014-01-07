package org.badiff.alg;

import java.util.Arrays;

import org.badiff.Op;
import org.badiff.q.OpQueue;

public class MinNextEditGraph extends EditGraph {
	
	protected int minNext = 1;

	public MinNextEditGraph(int size) {
		this(size, 1);
	}
	
	public MinNextEditGraph(int size, int minNext) {
		super(size);
		this.minNext = minNext;
	}
	
	@Override
	public void compute(byte[] orig, byte[] target) {
		xval = new byte[orig.length + 1];
		yval = new byte[target.length + 1];
		
		System.arraycopy(orig, 0, xval, 1, orig.length);
		System.arraycopy(target, 0, yval, 1, target.length);
		
		// mark short next runs as unusable
		Arrays.fill(flags, (byte) -1);
		for(int y = 0; y < yval.length; y++) {
			for(int x = 0; x < xval.length; x++) {
				if(x == 0 || y == 0 || xval[x] != yval[y])
					continue;
				int pos = x + y * xval.length;
				
				if(flags[pos] == Op.NEXT || flags[pos] == Op.STOP)
					continue;
				
				int extent = 0;
				int ix = x;
				int iy = y;
				do {
					extent++;
					ix++;
					iy++;
					if(ix == xval.length || iy == yval.length)
						break;
				} while(xval[ix] == yval[iy]);
				
				while(--ix >= x && --iy >= y) {
					flags[ix + iy * xval.length] = (extent >= minNext) ? Op.NEXT : Op.STOP;
				}
			}
		}
		
		// "normal" compute
		for(int y = 0; y < yval.length; y++) {
			for(int x = 0; x < xval.length; x++) {
				if(x == 0 && y == 0)
					continue;
				int pos = x + y * xval.length;
				
				// check for equality, but ensure that if equal not marked as too short
				if(x > 0 && y > 0 && flags[pos] == Op.NEXT) {
					lengths[pos] = (short) (1 + lengths[pos - xval.length - 1]);
					continue;
				}
				short dlen = x > 0 ? (short)(1 + lengths[pos-1]) : Short.MAX_VALUE;
				short ilen = y > 0 ? (short)(1 + lengths[pos - xval.length]) : Short.MAX_VALUE;
				if(dlen <= ilen) {
					flags[pos] = Op.DELETE;
					lengths[pos] = dlen;
				} else {
					flags[pos] = Op.INSERT;
					lengths[pos] = ilen;
				}
				
			}
		}
	}

	public int getMinNext() {
		return minNext;
	}

	public void setMinNext(int minNext) {
		if(minNext < 1)
			throw new IllegalArgumentException();
		this.minNext = minNext;
	}
	
}
