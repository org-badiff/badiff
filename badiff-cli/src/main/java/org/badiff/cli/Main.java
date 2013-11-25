package org.badiff.cli;

import java.util.Arrays;

import org.apache.commons.cli.Options;

public class Main {
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			help();
			System.exit(0);
		}
		
		if("diff".equals(args[0]))
			DiffMain.main(Arrays.copyOfRange(args, 1, args.length));
			
	}
	
	private static void help() {
		// TODO Auto-generated method stub

	}

}
