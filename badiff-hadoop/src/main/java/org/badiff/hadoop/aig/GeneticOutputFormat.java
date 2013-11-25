package org.badiff.hadoop.aig;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class GeneticOutputFormat extends TextOutputFormat<NullWritable, CsvGeneticParams> {

}
