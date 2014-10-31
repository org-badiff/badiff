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
package org.badiff;

import org.badiff.imp.MemoryDiff;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.util.Diffs;
import org.junit.Assert;
import org.junit.Test;

public class DiffsTest {

	@Test
	public void testDiff() throws Exception {
		String orig = "Hello world!";
		String target = "Hellish cruel world!";
		
		OpQueue diff = Diffs.queue(orig.getBytes(), target.getBytes());
		diff = new GraphOpQueue(diff, 1024);
		
		MemoryDiff md = new MemoryDiff(diff);
		
		byte[] result = Diffs.apply(md, orig.getBytes());
		
		
		Assert.assertEquals(target, new String(result));
	}

	@Test
	public void testDiffFromEmpty() throws Exception {
		String orig = "";
		String target = "Hellish cruel world!";
		
		OpQueue diff = Diffs.queue(orig.getBytes(), target.getBytes());
		diff = new GraphOpQueue(diff, 1024);
		
		MemoryDiff md = new MemoryDiff(diff);
		
		byte[] result = Diffs.apply(md, orig.getBytes());
		
		
		Assert.assertEquals(target, new String(result));
	}
	
}
