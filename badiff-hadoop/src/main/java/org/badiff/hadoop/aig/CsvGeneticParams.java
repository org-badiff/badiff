package org.badiff.hadoop.aig;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.badiff.Op;
import org.badiff.alg.AdjustableInertialGraph;

public class CsvGeneticParams implements WritableComparable<CsvGeneticParams>, Cloneable {

	private double score;
	private double[] params;
	
	public CsvGeneticParams() {}
	
	public CsvGeneticParams(double score, double[] params) {
		this.score = score;
		this.params = params;
		normalize();
	}
	
	public CsvGeneticParams(double score, CsvGeneticParams params) {
		this.score = score;
		this.params = params.getParamsCopy();
		normalize();
	}
	
	public void normalize() {
		double psum = 0;
		for(double p : params)
			psum += p;
		for(int i = 0; i < params.length; i++)
			params[i] /= psum;
	}
	
	@Override
	public String toString() {
		return score + " <= " + Arrays.toString(params);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(params);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(obj instanceof CsvGeneticParams)
			return Arrays.equals(params, ((CsvGeneticParams) obj).params);
		return false;
	}
	
	public void applyTo(AdjustableInertialGraph graph) {
		for(byte f = Op.STOP; f <= Op.NEXT; f++)
			for(byte t = Op.STOP; t <= Op.NEXT; t++)
				graph.setCost(f, t, (float) params[f * 4 + t]);
	}
	
	public static CsvGeneticParams read(DataInput in) throws IOException {
		CsvGeneticParams p = new CsvGeneticParams();
		p.readFields(in);
		p.normalize();
		return p;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(score);
		for(double p : params) {
			sb.append(",");
			sb.append(p);
		}
		sb.append("\n");
		out.writeBytes(sb.toString());
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		StringBuilder sb = new StringBuilder();
		char c;
		for(c = (char) in.readUnsignedByte(); c != '\n'; c = (char) in.readUnsignedByte()) {
			sb.append(c);
		}
		readString(sb.toString());
	}
	
	public void readString(String s) {
		String[] fields = s.trim().split("[^0-9\\.]+");
		score = Double.parseDouble(fields[0]);
		params = new double[fields.length - 1];
		for(int i = 0; i < params.length; i++)
			params[i] = Double.parseDouble(fields[i+1]);
		normalize();
	}

	public double getScore() {
		return score;
	}

	public double[] getParamsCopy() {
		return Arrays.copyOf(params, params.length);
	}

	@Override
	public int compareTo(CsvGeneticParams o) {
		// order descending
		return -((Double) score).compareTo(o.score);
	}

	@Override
	public CsvGeneticParams clone() {
		try {
			return (CsvGeneticParams) super.clone();
		} catch(CloneNotSupportedException e) {
			throw new InternalError("clone not supported?");
		}
	}
}
