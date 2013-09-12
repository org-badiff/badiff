package org.badiff.alg;

import java.util.Arrays;

import org.badiff.imp.MemoryDiff;
import org.badiff.util.Diffs;
import org.junit.Assert;
import org.junit.Test;

public class IntertialGraphTest {

	@Test
	public void testGraph() {
		byte[] orig = "Hello world!".getBytes();
		byte[] target = "Hellish cruel world!".getBytes();
		
		InnertialGraph ig = new InnertialGraph((orig.length + 1) * (target.length + 1));
		ig.compute(orig, target);
		
		MemoryDiff md = new MemoryDiff(ig.queue());
		System.out.println(md);
		
		byte[] result = Diffs.apply(md, orig);
		System.out.println(new String(result));
		
		Assert.assertEquals(new String(target), new String(result));
		
		Graph g = new Graph((orig.length + 1) * (target.length + 1));
		g.compute(orig, target);
		
		md.store(g.queue());
		System.out.println(md);
	}

}
