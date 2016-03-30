package org.badiff.exa;

import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.p.Pipeline;
import org.badiff.p.Pipes;
import org.badiff.q.OpQueue;
import org.badiff.q.ReplaceOpQueue;
import org.badiff.util.Serials;

public class UsingPipelines {
	/**
	 * Custom diff that uses pipelines
	 * @param orig The original byte sequence
	 * @param target The target byte sequence
	 * @return The diff, as a byte sequence
	 */
	public static byte[] finelyTunedDiff(byte[] orig, byte[] target) {
		// Create an OpQueue with only tqo operations: delete orig and insert target
		OpQueue q = new ReplaceOpQueue(orig, target);
		
		// Use a pipeline to construct an OpQueue chain for the diff
		/* 
		 * NOTE:
		 * Just constructing a pipeline will not cause the OpQueue chain
		 * to start processing.
		 */
		
		Pipeline pipeline = Pipes.SPLIT.from(q) // Split it into chunks processable by the edit graphs
				.into(Pipes.GRAPH) // Single-threaded edit graphing
				.into(Pipes.UNCHUNK) // Reorder inserts and deletes where possible
				.into(Pipes.COMPACT) // Combine individual but sequential operations into single operations
				.into(Pipes.COALESS) // Coalesce identical inserts and deletes into NEXT
				.into(Pipes.ONE_WAY); // Strip reversable data
		
		// Return a new diff stored in memory
		/*
		 * NOTE:
		 * MemoryDiff's constructor will pull all the operations off the pipeline,
		 * and so the diff will be computed here.
		 */
		MemoryDiff diff = new MemoryDiff(pipeline.outlet());
		
		// Serialize the diff to a byte[]
		return Serials.serialize(DefaultSerialization.newInstance(), MemoryDiff.class, diff);
	}
}
