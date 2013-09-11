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
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.util.Arrays;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.imp.BadiffFileDiff.Optional;
import org.badiff.io.DataInputInputStream;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.NoopOutputStream;
import org.badiff.io.RandomInput;
import org.badiff.io.RandomInputStream;
import org.badiff.q.OpQueue;
import org.badiff.util.Digests;
import org.badiff.util.Streams;

public class BadiffFormat implements InputFormat, OutputFormat {

	@Override
	public void exportDiff(Diff diff, RandomInput orig, DataOutput out)
			throws IOException {
		Optional opt = new Optional();
		
		long opos = orig.position();
		
		opt.setHashAlgorithm(Digests.defaultDigest().getAlgorithm());
		DigestInputStream digin = new DigestInputStream(new RandomInputStream(orig), Digests.defaultDigest());
		DigestOutputStream digout = new DigestOutputStream(new NoopOutputStream(), Digests.defaultDigest());
		
		diff.apply(digin, digout);
		
		orig.seek(opos);
		
		opt.setPreHash(digin.getMessageDigest().digest());
		opt.setPostHash(digout.getMessageDigest().digest());
		
		BadiffFileDiff.store(out, DefaultSerialization.getInstance(), opt, diff.queue());
		
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
		
		Optional opt = bd.header().getOptional();
		if(opt != null) {
			if(opt.getHashAlgorithm() != null && opt.getPreHash() != null) {
				long opos = orig.position();
				DigestInputStream digin = new DigestInputStream(
						new DataInputInputStream(orig),
						Digests.digest(opt.getHashAlgorithm()));
				Streams.copy(digin, new NoopOutputStream());
				orig.seek(opos);
				byte[] actualPreHash = digin.getMessageDigest().digest();
				if(!Arrays.equals(opt.getPreHash(), actualPreHash))
					throw new IOException("Pre-hash mismatch, expected " + Arrays.toString(opt.getPreHash()) + ", found " + Arrays.toString(actualPreHash));
			}
		}

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
