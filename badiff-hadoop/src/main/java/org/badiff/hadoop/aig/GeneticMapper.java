package org.badiff.hadoop.aig;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.badiff.Diff;
import org.badiff.alg.AdjustableInertialGraph;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.NoopOutputStream;
import org.badiff.q.CoalescingOpQueue;
import org.badiff.q.CompactingOpQueue;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.RewindingOpQueue;
import org.badiff.q.StreamChunkingOpQueue;
import org.badiff.q.UnchunkingOpQueue;

public class GeneticMapper extends Mapper<NullWritable, CsvGeneticParams, FloatWritable, CsvGeneticParams> {
	private static final String PREFIX = GeneticMapper.class.getName() + ".";
	private static final String REPETITIONS = PREFIX + "repetitions";
	private static final String SEED = PREFIX + "seed";
	private static final String ITERATION = PREFIX + "iteration";
	private static final Logger log = Logger.getLogger(GeneticMapper.class);
	
	public static void setRepetitions(Job job, int repetitions) {
		job.getConfiguration().setInt(REPETITIONS, repetitions);
	}
	
	public static void setSeed(Job job, long seed) {
		job.getConfiguration().setLong(SEED, seed);
	}
	
	public static void setIteration(Job job, int iteration) {
		job.getConfiguration().setInt(ITERATION, iteration);
	}
	
	public static int getRepetitions(Configuration conf) {
		return conf.getInt(REPETITIONS, 4);
	}
	
	public static long getSeed(Configuration conf) {
		return conf.getLong(SEED, 0xdeadbeef);
	}
	
	public static int getIteration(Configuration conf) {
		return conf.getInt(ITERATION, 0);
	}

	private int repetitions;
	private long seed;
	private int iteration;
	private AdjustableInertialGraph graph;

	@Override
	protected void setup(Context context)
			throws IOException, InterruptedException {
		repetitions = getRepetitions(context.getConfiguration());
		seed = getSeed(context.getConfiguration());
		iteration = getIteration(context.getConfiguration());
		graph = new AdjustableInertialGraph((1+Diff.DEFAULT_CHUNK)*(1+Diff.DEFAULT_CHUNK));
	}

	@Override
	protected void map(NullWritable key, CsvGeneticParams value, Context context)
			throws IOException, InterruptedException {
		log.debug("Evaluating " + value);
		System.out.println("Evaluating " + value);
		CsvGeneticParams out = eval(context, value);
		context.write(new FloatWritable((float) out.getScore()), out);
	}

	protected CsvGeneticParams eval(Context context, final CsvGeneticParams in) {
		double score = 0;

		in.applyTo(graph);

		for(int r = 0; r < repetitions; r++) {
			log.debug("Evaluating iteration " + r + " of " + repetitions + " for " + in);
			
			InputBytesGenerator bytes = new InputBytesGenerator(seed * r, iteration);
			
			OpQueue q;
			q = new StreamChunkingOpQueue(
					new ByteArrayInputStream(bytes.getFrom()), 
					new ByteArrayInputStream(bytes.getTo()));
			q = new GraphOpQueue(q, graph);
			q = new CoalescingOpQueue(q);
			q = new RewindingOpQueue(q);
			q = new OneWayOpQueue(q);
			q = new UnchunkingOpQueue(q);
			q = new CompactingOpQueue(q);
			
			MemoryDiff diff = new MemoryDiff(q);
			NoopOutputStream nout = new NoopOutputStream();
			try {
				diff.serialize(DefaultSerialization.newInstance(), new DataOutputStream(nout));
			} catch(IOException ioe) {
				throw new RuntimeException(ioe);
			}
			
			score += nout.getBytesWritten();
		}
		return new CsvGeneticParams(score / repetitions, in);
	}
}
