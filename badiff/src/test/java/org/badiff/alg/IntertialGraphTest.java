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
package org.badiff.alg;

import java.io.IOException;
import java.util.Arrays;

import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.ReplaceOpQueue;
import org.badiff.util.Diffs;
import org.badiff.util.Serials;
import org.junit.Assert;
import org.junit.Test;

public class IntertialGraphTest {

	@Test
	public void testGraph() {
		byte[] orig = "Hello world!".getBytes();
		byte[] target = "Hellish cruel world!".getBytes();
		
		InertialGraph ig = new InertialGraph((orig.length + 1) * (target.length + 1));
		ig.compute(orig, target);
		
		MemoryDiff imd = new MemoryDiff(new OneWayOpQueue(ig.queue()));
		System.out.println(imd);
		
		byte[] result = Diffs.apply(imd, orig);
		System.out.println(new String(result));
		
		Assert.assertEquals(new String(target), new String(result));
		
		EditGraph g = new EditGraph((orig.length + 1) * (target.length + 1));
		g.compute(orig, target);
		
		MemoryDiff emd = new MemoryDiff(g.queue());
		System.out.println(emd);
		
		byte[] simd = Serials.serialize(DefaultSerialization.getInstance(), MemoryDiff.class, imd);
		byte[] semd = Serials.serialize(DefaultSerialization.getInstance(), MemoryDiff.class, emd);
		
		System.out.println("inertial diff length:" + simd.length);
		System.out.println("edit diff length:" + semd.length);
	}

	@Test
	public void testGraphOpQueue() throws IOException {
		byte[] orig = "Hello world!".getBytes();
		byte[] target = "Hellish cruel world!".getBytes();
		
		InertialGraph ig = new InertialGraph((orig.length + 1) * (target.length + 1));
		
		OpQueue q = new ReplaceOpQueue(orig, target);
		q = new GraphOpQueue(q, ig);
		q = new OneWayOpQueue(q);
		
		MemoryDiff md = new MemoryDiff(q);
		System.out.println(md);
	}
	
}
