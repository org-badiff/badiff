package org.badiff.cli;

import static org.badiff.cli.Arguments.DiffArguments.AFTER;
import static org.badiff.cli.Arguments.DiffArguments.BEFORE;
import static org.badiff.cli.Arguments.DiffArguments.OUTPUT;
import static org.badiff.cli.Arguments.DiffArguments.PIPELINE;
import static org.badiff.cli.Arguments.DiffArguments.VERBOSE;
import static org.badiff.cli.Arguments.DiffArguments.CHUNK;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.cli.CommandLine;
import org.badiff.alg.GraphFactory;
import org.badiff.cli.io.ListenableRandomInput;
import org.badiff.cli.io.ProgressInputListener;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.imp.BadiffFileDiff.Header;
import org.badiff.imp.FileDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.FileRandomInput;
import org.badiff.p.Pipeline;
import org.badiff.q.OpQueue;
import org.badiff.q.ParallelGraphOpQueue;
import org.badiff.q.RandomChunkingOpQueue;
import org.badiff.util.Digests;

public class DiffMain {
	
	public static void main(String[] args) throws Exception {
		CommandLine cli = Arguments.DIFF.parse(args);
		
		File orig = new File(cli.getOptionValue(BEFORE));
		File target = new File(cli.getOptionValue(AFTER));
		BadiffFileDiff output = new BadiffFileDiff(cli.getOptionValue(OUTPUT));
		String pipeline = cli.getOptionValue(PIPELINE);
		int chunk = Integer.parseInt(cli.getOptionValue(CHUNK));

		ListenableRandomInput oin = new ListenableRandomInput(new FileRandomInput(orig));
		ListenableRandomInput tin = new ListenableRandomInput(new FileRandomInput(target));
		
		if(cli.hasOption(VERBOSE))
			new ProgressInputListener(oin, tin);
		
		byte[] preHash = Digests.digest(orig, Digests.defaultDigest());
		byte[] postHash = Digests.digest(target, Digests.defaultDigest());
		
		FileDiff tmp = new FileDiff(output.getParentFile(), output.getName() + ".tmp");
		
		OpQueue q;
		q = new RandomChunkingOpQueue(oin, tin, chunk);
		
		q = new ParallelGraphOpQueue(q, Runtime.getRuntime().availableProcessors(), chunk, GraphFactory.INERTIAL_GRAPH);
		
		q = new Pipeline(q).into(pipeline).outlet();
		
		tmp.store(q);
		
		Header h = new Header();
		
		Header.Optional opt = new Header.Optional();
		h.setOptional(opt);
		opt.setHashAlgorithm(Digests.defaultDigest().getAlgorithm());
		opt.setPreHash(preHash);
		opt.setPostHash(postHash);
		
		DataOutputStream self = new DataOutputStream(new FileOutputStream(output));
		BadiffFileDiff.store(self, DefaultSerialization.newInstance(), h, tmp.queue());
		self.close();
		
		tmp.delete();

	}
}
