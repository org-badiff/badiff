package org.badiff.cli;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.badiff.cli.io.ListenableRandomInput;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.imp.FileDiff;
import org.badiff.imp.BadiffFileDiff.Header;
import org.badiff.imp.BadiffFileDiff.Header.Optional;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.FileRandomInput;
import org.badiff.p.Pipeline;
import org.badiff.q.OpQueue;
import org.badiff.q.RandomChunkingOpQueue;
import org.badiff.q.StreamChunkingOpQueue;
import org.badiff.util.Digests;

public class DiffMain {
	public static final String PIPELINE = "pipeline";
	public static final String OUTPUT = "output";
	public static final String BEFORE = "before";
	public static final String AFTER = "after";
	
	private static final Options OPT = new Options(DiffMain.class.getResource("DiffMain_options.properties"));
	static {
		OPT.optional("p", PIPELINE, true, "diff pipeline code to use");
		OPT.required("o", OUTPUT, true, "output file");
		OPT.required("1", BEFORE, true, "original file for comparison");
		OPT.required("2", AFTER, true, "target file for comparison");
	}
	
	public static void main(String[] args) throws Exception {
		CommandLine cli = OPT.parse(args);
		
		File orig = new File(cli.getOptionValue(BEFORE));
		File target = new File(cli.getOptionValue(AFTER));
		BadiffFileDiff output = new BadiffFileDiff(cli.getOptionValue(OUTPUT));
		String pipeline = cli.getOptionValue(PIPELINE);

		ListenableRandomInput oin = new ListenableRandomInput(new FileRandomInput(orig));
		ListenableRandomInput tin = new ListenableRandomInput(new FileRandomInput(target));
		
		new InputProgress(oin, tin);

		output.diff(oin, tin, pipeline);
	}
}
