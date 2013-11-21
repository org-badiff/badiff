package org.badiff.hadoop.aig;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.badiff.hadoop.aig.GeneticInputFormat.GeneticRecordReader;

public class AdjustableInertialGraphJob extends Job {

	private static final String SPLIT_SIZE = "split-size";
	private static final String ITERATIONS = "iterations";
	private static final String PERMUTATIONS = "permutations";
	private static final String REPETITIONS = "repetitions";
	private static final String SURVIVORS = "survivors";
	private static final String SEED = "seed";
	private static final String PATH_PREFIX = "path";
	private static final String RESUME = "resume";
	private static final String JOB_NAME = "name";
	private static final String RANDOM_SEED = "random-seed";
	
	private static Options opt = new Options();
	static {
		opt.addOption(null, SPLIT_SIZE, true, "size of an input split");
		opt.addOption(null, ITERATIONS, true, "number of iterations");
		opt.addOption(null, PERMUTATIONS, true, "number of permutations for each input");
		opt.addOption(null, REPETITIONS, true, "number of repetitions for each permutation");
		opt.addOption(null, SURVIVORS, true, "number of survivors per generation");
		opt.addOption(null, SEED, true, "data input path");
		opt.addOption(null, PATH_PREFIX, true, "data out path prefix");
		opt.addOption(null, RESUME, true, "iteration at which to resume");
		opt.addOption(null, JOB_NAME, true, "name prefix of the job");
		opt.addOption(null, RANDOM_SEED, true, "random seed value (long)");
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		CommandLine cli = new GenericOptionsParser(conf, opt, args).getCommandLine();
		for(Option opt : cli.getOptions()) {
			if(opt.getLongOpt() == null || opt.getValue() == null)
				continue;
			conf.set(opt.getLongOpt(), opt.getValue());
		}
		
		Logger.getLogger("org.eviline.mapr").setLevel(Level.ALL);
		
		for(int i = conf.getInt(RESUME, 0); i < conf.getInt(ITERATIONS, 1); i++) {
			AdjustableInertialGraphJob job = new AdjustableInertialGraphJob(conf, i);
			job.submit();
			job.waitForCompletion(true);
		}
	}
	
	public AdjustableInertialGraphJob(Configuration conf, int iteration) throws IOException {
		super(conf);

		Path prefix = new Path(conf.get(PATH_PREFIX));
		Path seed = new Path(conf.get(SEED));
		int permutations = conf.getInt(PERMUTATIONS, 20);
		int repetitions = conf.getInt(REPETITIONS, 3);
		int survivors = conf.getInt(SURVIVORS, 1000);
		long randomSeed = Long.decode(conf.get(RANDOM_SEED));
		
		setJarByClass(getClass());
		setJobName(conf.get(JOB_NAME, getClass().getName()) + "_" + iteration);
		
		setInputFormatClass(GeneticInputFormat.class);
		setMapperClass(GeneticMapper.class);
		setMapOutputKeyClass(FloatWritable.class);
		setMapOutputValueClass(CsvGeneticParams.class);
		setReducerClass(GeneticReducer.class);
		setNumReduceTasks(1);
		setOutputFormatClass(GeneticOutputFormat.class);
		setOutputKeyClass(NullWritable.class);
		setOutputValueClass(CsvGeneticParams.class);
		
		GeneticInputFormat.setMinInputSplitSize(this, conf.getLong(SPLIT_SIZE, 80 * 100));
		GeneticInputFormat.setMaxInputSplitSize(this, conf.getLong(SPLIT_SIZE, 80 * 100));
		
		if(iteration > 0)
			GeneticInputFormat.addInputPath(this, new Path(prefix, "" + iteration + "/*"));
		else
			GeneticInputFormat.addInputPath(this, seed);
		
		GeneticRecordReader.setPermutations(this, permutations);
		
		GeneticMapper.setRepetitions(this, repetitions);
		GeneticMapper.setSeed(this, randomSeed);
		GeneticMapper.setIteration(this, iteration);
		
		GeneticReducer.setSurvivorSize(this, survivors);
		
		GeneticOutputFormat.setOutputPath(this, new Path(prefix, "" + (iteration + 1)));
	}

}
