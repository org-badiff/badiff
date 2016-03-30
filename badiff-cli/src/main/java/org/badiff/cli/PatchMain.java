package org.badiff.cli;

import static org.badiff.cli.Arguments.PatchArguments.AFTER;
import static org.badiff.cli.Arguments.PatchArguments.BEFORE;
import static org.badiff.cli.Arguments.PatchArguments.PATCH;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.util.Diffs;

public class PatchMain {

	public static void main(String[] args) throws Exception {
		CommandLine cli = Arguments.PATCH.parse(args);
		
		File orig = new File(cli.getOptionValue(BEFORE));
		File target = new File(cli.getOptionValue(AFTER));
		BadiffFileDiff patch = new BadiffFileDiff(cli.getOptionValue(PATCH));

		Diffs.apply(patch, orig, target);
		
	}

}
