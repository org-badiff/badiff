package org.badiff.alg;

import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.q.OneWayOpQueue;
import org.badiff.util.Diffs;
import org.badiff.util.Serials;
import org.junit.Assert;
import org.junit.Test;

public class MinNextEditGraphTest {
	@Test
	public void testGraph() {
		byte[] orig = "Hello world!".getBytes();
		byte[] target = "Hellish cruel world!".getBytes();
		
		MinNextEditGraph ig = new MinNextEditGraph((orig.length + 1) * (target.length + 1));
		ig.setMinNext(5);
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
		
		byte[] simd = Serials.serialize(DefaultSerialization.newInstance(), MemoryDiff.class, imd);
		byte[] semd = Serials.serialize(DefaultSerialization.newInstance(), MemoryDiff.class, emd);
		
		System.out.println("minnext diff length:" + simd.length);
		System.out.println("edit diff length:" + semd.length);
	}

}
