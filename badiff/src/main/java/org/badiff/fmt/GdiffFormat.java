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
package org.badiff.fmt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.q.OpQueue;

public class GdiffFormat implements OutputFormat {

	public GdiffFormat() {
	}
	
	@Override
	public void export(Diff diff, ByteBuffer orig, DataOutput out) throws IOException {
		OpQueue q = diff.queue();
		long opos = 0;
		for(Op e = q.poll(); e != null; e = q.poll()) {
			switch(e.getOp()) {
			case Op.DELETE:
				opos += e.getRun();
				break;
			case Op.INSERT:
				data(out, e.getData());
				break;
			case Op.NEXT:
				copy(out, e.getRun(), opos);
				opos += e.getRun();
				break;
			}
		}
		eof(out);
	}
	
	private void eof(DataOutput out) throws IOException {
		out.writeByte(0);
	}
	
	private void data(DataOutput out, byte[] buf) throws IOException {
		if(buf.length <= 246) {
			out.writeByte(buf.length);
			out.write(buf);
			return;
		}
		if(buf.length < 65536) {
			out.writeByte(247);
			out.writeShort(buf.length);
			out.write(buf);
			return;
		}
		out.writeByte(248);;
		out.writeInt(buf.length);
		out.write(buf);
	}
	
	private void copy(DataOutput out, int run, long pos) throws IOException {
		if(pos < 65536) {
			if(run < 256) {
				out.writeByte(249);
				out.writeShort((int) pos);
				out.writeByte(run);
				return;
			}
			if(run < 65536) {
				out.writeByte(250);
				out.writeShort((int) pos);
				out.writeShort(run);
				return;
			}
			out.writeByte(251);
			out.writeShort((int) pos);
			out.writeInt(run);
			return;
		}
		if(pos <= Integer.MAX_VALUE) {
			if(run < 252) {
				out.writeByte(249);
				out.writeInt((int) pos);
				out.writeByte(run);
				return;
			}
			if(run < 65536) {
				out.writeByte(253);
				out.writeInt((int) pos);
				out.writeShort(run);
				return;
			}
			out.writeByte(254);
			out.writeInt((int) pos);
			out.writeInt(run);
			return;
		}
		out.writeByte(255);
		out.writeLong(pos);
		out.writeInt(run);
	}

}
