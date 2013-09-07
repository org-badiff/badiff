package org.badiff.nat;

import java.util.List;

import org.badiff.Op;
import org.badiff.alg.Graph;

public class NativeBadiffGraph extends Graph {
	protected NativeGraph graph;
	
	public NativeBadiffGraph(int bufSize) {
		super(1);
		graph = new NativeGraph(bufSize);
	}
	
	@Override
	public void compute(byte[] orig, byte[] target) {
		graph.compute(orig, target);
	}
	
	@Override
	public List<Op> rlist() {
		return graph.rlist();
	}
}
