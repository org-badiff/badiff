package org.badiff.cli;

import java.util.Arrays;

public class Main {
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			help(args);
			System.exit(0);
		}
		
		if("help".equals(args[0]))
			help(args);
		else if("diff".equals(args[0]))
			DiffMain.main(Arrays.copyOfRange(args, 1, args.length));
		else if("patch".equals(args[0]))
			PatchMain.main(Arrays.copyOfRange(args, 1, args.length));
		else
			help(args);
	}
	
	private static void help(String[] args) {
		if(args.length == 0) {
			p("badiff-cli command [ARGS...]");
			p("Valid commands:");
			p("\thelp [command]");
			p("\tdiff -1 orig -2 target -o patch [-v]");
			p("\tpatch -1 orig -p patch -2 target [-v]");
		} else if("help".equals(args[0])) {
			p("badiff-cli help [command]");
			p("Shows help for all commands, or the specific command if specified");
		} else if("diff".equals(args[0])) {
			p("badiff-cli diff -1 orig -2 target -o patch [-v]");
			p("\t-1\tThe original file");
			p("\t-2\tThe modified file");
			p("\t-o\tThe patch file to generate (created)");
			p("\t-v\tBe verbose");
		} else if("patch".equals(args[0])) {
			p("badiff-cli patch -1 orig -p patch -o target [-v]");
			p("\t-1\tThe original file");
			p("\t-o\tThe modified file (created)");
			p("\t-p\tThe patch file");
			p("\t-v\tBe verbose");
		} else {
			p("Unrecognized command:" + args[0]);
		}
	}
	
	private static void p(Object s) {
		System.out.println(s);
	}

}
