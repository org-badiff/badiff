package org.badiff.cli;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.badiff.imp.BadiffFileDiff;
import static org.badiff.cli.Arguments.DiffArguments.*;

public class DiffMain {
	
	public static void main(String[] args) throws Exception {
		CommandLine cli = Arguments.DIFF.parse(args);
		
		File orig = new File(cli.getOptionValue(BEFORE));
		File target = new File(cli.getOptionValue(AFTER));
		BadiffFileDiff output = new BadiffFileDiff(cli.getOptionValue(OUTPUT));
		String pipeline = cli.getOptionValue(PIPELINE);

		output.diff(orig, target, pipeline);
	}
}
