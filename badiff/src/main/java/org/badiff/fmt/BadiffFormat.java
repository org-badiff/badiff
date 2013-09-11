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

import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.io.DataInputInputStream;
import org.badiff.io.DataOutputOutputStream;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.RandomInput;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.io.SmallNumberSerialization;
import org.badiff.q.OpQueue;
import org.badiff.util.Streams;

public class BadiffFormat implements InputFormat, OutputFormat {

	@Override
	public void exportDiff(Diff diff, RandomInput orig, DataOutput out)
			throws IOException {
		DataOutputOutputStream dout = new DataOutputOutputStream(out);
		BadiffFileDiff bd = new BadiffFileDiff(File.createTempFile("badiff", ".tmp"));
		
		bd.store(diff.queue());
		
		FileInputStream bdi = new FileInputStream(bd);
		Streams.copy(bdi, dout);
		bdi.close();
		
		bd.delete();
	}

	@Override
	public OpQueue importDiff(RandomInput orig, RandomInput ext)
			throws IOException {
		DataInputInputStream din = new DataInputInputStream(ext);
		BadiffFileDiff bd = new BadiffFileDiff(File.createTempFile("badiff", ".tmp"));
		bd.deleteOnExit();
		
		FileOutputStream bdo = new FileOutputStream(bd);
		Streams.copy(din, bdo);
		bdo.close();

		return new BadiffFormatOpQueue(bd);
		
	}

	private class BadiffFormatOpQueue extends OpQueue {
		private BadiffFileDiff bd;
		private OpQueue q;
	
		private BadiffFormatOpQueue(BadiffFileDiff bd) throws IOException {
			this.bd = bd;
			q = bd.queue();
		}
	
		@Override
		protected boolean pull() {
			Op e = q.poll();
			if(e != null)
				prepare(e);
			else
				bd.delete();
			return e != null;
		}
	}
	
	

}
