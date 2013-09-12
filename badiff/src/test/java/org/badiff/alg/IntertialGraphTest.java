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
		
		InnertialGraph ig = new InnertialGraph((orig.length + 1) * (target.length + 1));
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
		
		InnertialGraph ig = new InnertialGraph((orig.length + 1) * (target.length + 1));
		
		OpQueue q = new ReplaceOpQueue(orig, target);
		q = new GraphOpQueue(q, ig);
		q = new OneWayOpQueue(q);
		
		MemoryDiff md = new MemoryDiff(q);
		System.out.println(md);
	}
	
}
