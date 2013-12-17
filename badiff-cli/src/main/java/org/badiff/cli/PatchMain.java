package org.badiff.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.util.Diffs;

import static org.badiff.cli.Arguments.PatchArguments.*;

public class PatchMain {

	public static void main(String[] args) throws Exception {
		CommandLine cli = Arguments.PATCH.parse(args);
		
		File orig = new File(cli.getOptionValue(BEFORE));
		File target = new File(cli.getOptionValue(AFTER));
		BadiffFileDiff patch = new BadiffFileDiff(cli.getOptionValue(PATCH));

		Diffs.apply(patch, orig, target);
		
	}

}
