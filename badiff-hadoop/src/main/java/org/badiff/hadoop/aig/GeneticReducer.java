package org.badiff.hadoop.aig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

public class GeneticReducer extends Reducer<FloatWritable, CsvGeneticParams, NullWritable, CsvGeneticParams> {
	
	private static final String PREFIX = GeneticReducer.class.getName() + ".";
	private static final String SURVIVORS = PREFIX + "survivors";
	
	private static final Logger log = Logger.getLogger(GeneticReducer.class);
	
	public static void setSurvivorSize(Job job, int survivorSize) {
		job.getConfiguration().setInt(SURVIVORS, survivorSize);
	}
	
	private PriorityQueue<CsvGeneticParams> survivors = new PriorityQueue<CsvGeneticParams>();
	private int survivorSize;
	
	@Override
	protected void setup(Context context)
			throws IOException, InterruptedException {
		survivorSize = context.getConfiguration().getInt(SURVIVORS, 1000);
	}
	
	@Override
	protected void reduce(FloatWritable key, Iterable<CsvGeneticParams> values, Context context)
			throws IOException, InterruptedException {
		for(CsvGeneticParams p : values) {
			survivors.offer(p.clone());
			while(survivors.size() > survivorSize)
				survivors.poll();
		}
	}
	
	@Override
	protected void cleanup(Context context)
			throws IOException, InterruptedException {
		List<CsvGeneticParams> s = new ArrayList<CsvGeneticParams>();
		while(survivors.size() > 0) {
			s.add(survivors.poll());
		}
		Collections.reverse(s);
		for(CsvGeneticParams p : s) {
			context.write(NullWritable.get(), p);
			log.info(p);
		}
	}
}
