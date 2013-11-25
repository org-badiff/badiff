package org.badiff.hadoop.aig;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

public class GeneticInputFormat extends FileInputFormat<NullWritable, CsvGeneticParams> {

	@Override
	public RecordReader<NullWritable, CsvGeneticParams> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		return new GeneticRecordReader();
	}
	
	public static class GeneticRecordReader extends RecordReader<NullWritable, CsvGeneticParams> {
		private LineRecordReader lines = new LineRecordReader();
		private CsvGeneticParams current;
		private int permutations;
		private Deque<CsvGeneticParams> q = new ArrayDeque<CsvGeneticParams>();
		private int maxq;
		
		private static final String PREFIX = GeneticInputFormat.class.getName() + ".";
		private static final String PERMUTATIONS = PREFIX + "permutations";
		
		public static void setPermutations(Job job, int permutations) {
			job.getConfiguration().setInt(PERMUTATIONS, permutations);
		}
		
		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			permutations = context.getConfiguration().getInt(PERMUTATIONS, 20);

			lines.initialize(split, context);
			Deque<String> lq = new ArrayDeque<String>();
			while(lines.nextKeyValue()) {
				lq.offer(lines.getCurrentValue().toString());
			}
			lines.close();
			for(String line : lq) {
				CsvGeneticParams seed = new CsvGeneticParams();
				try {
					seed.readString(line);
					q.addAll(permute(seed));
				} catch(NumberFormatException nfe) {
				}
			}
			maxq = q.size();
			
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			if(q.size() > 0) {
				current = q.poll();
				return true;
			}
			
			return false;
		}

		@Override
		public NullWritable getCurrentKey() throws IOException,
				InterruptedException {
			return NullWritable.get();
		}

		@Override
		public CsvGeneticParams getCurrentValue() throws IOException,
				InterruptedException {
			return current;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			return Math.max(0f, 1 - q.size() / (float) maxq);
		}

		@Override
		public void close() throws IOException {
		}

		protected double pnRandom() {
			return Math.pow(Math.random(), 4) - Math.pow(Math.random(), 4);
		}
		
		protected List<CsvGeneticParams> permute(CsvGeneticParams in) {
			List<CsvGeneticParams> ret = new ArrayList<CsvGeneticParams>();
			ret.add(new CsvGeneticParams(Double.NaN, in.getParamsCopy()));
			for(int i = 0; i < permutations; i++) {
				double[] pd = in.getParamsCopy();
				for(int j = 0; j < pd.length; j++) {
					pd[j] *= (1 + pnRandom() * 0.1);
					pd[j] += pnRandom() * 0.05;
					pd[j] = Math.max(0, Math.min(1, pd[j]));
				}
				ret.add(new CsvGeneticParams(Double.NaN, pd));
			}
			return ret;
		}

	}

}
