/**
 * badiff - byte array diff - fast pure-java byte-level diffing
 * 
 * Copyright (c) 2013, Robin Kirkman All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3) Neither the name of the badiff nor the names of its contributors may be 
 *    used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.badiff.nat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.badiff.Op;

public class NativeGraph {
	private static final byte STOP = Op.STOP;
	private static final byte DELETE = Op.DELETE;
	private static final byte INSERT = Op.INSERT;
	private static final byte NEXT = Op.NEXT;
	
	static {
		try {
			System.loadLibrary("badiff-native-1.0.2-SNAPSHOT");
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	private native void new0(int bufSize);
	
	private native void compute0(byte[] orig, byte[] target);

	private native boolean walk0();
	private native byte flag0();
	private native byte val0();
	private native void prev0();
	
	private native void free0();
	
	private volatile long data;
	private int bufSize;
	
	public NativeGraph(int bufSize) {
		this.bufSize = bufSize;
		new0(bufSize);
	}
	
	public void compute(byte[] orig, byte[] target) {
		compute0(orig, target);
	}
	
	public List<Op> rlist() {
		if(!walk0())
			throw new IllegalStateException("Graph not computed");
		List<Op> ret = new ArrayList<Op>((int) Math.sqrt(bufSize));

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte op = Op.STOP;
		int run = 0;
		
		while(flag0() != Op.STOP) {
			byte fop = flag0();
			if(op != Op.STOP && op != fop) {
				byte[] data = null;
				if(op == Op.INSERT || op == Op.DELETE) {
					byte[] rdata = buf.toByteArray();
					data = new byte[rdata.length];
					for(int i = 0; i < rdata.length; i++) {
						data[data.length - i - 1] = rdata[i];
					}
				}
				ret.add(new Op(op, run, data));
				run = 0;
				buf.reset();
			}
			op = fop;
			run++;
			if(op == Op.INSERT) {
				buf.write(val0());
			}
			if(op == Op.DELETE) {
				buf.write(val0());
			}
			prev0();
		}
		
		if(op != Op.STOP) {
			byte[] data = null;
			if(op == Op.INSERT || op == Op.DELETE) {
				byte[] rdata = buf.toByteArray();
				data = new byte[rdata.length];
				for(int i = 0; i < rdata.length; i++) {
					data[data.length - i - 1] = rdata[i];
				}
			}
			ret.add(new Op(op, run, data));
		}

		return ret;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		free0();
	}
}