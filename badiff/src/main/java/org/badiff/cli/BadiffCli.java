/**
 * badiff - byte array diff - fast pure-java byte-level diffing
 * 
 * Copyright (c) 2013, Robin Kirkman All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3) Neither the name of the badiff nor the names of its contributors may be 
 *    used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.badiff.cli;

import java.io.File;

import org.badiff.imp.BadiffFileDiff;
import org.badiff.imp.BadiffFileDiff.Header;
import org.badiff.util.Digests;

/**
 * Command-line interface for badiff
 * @author robin
 *
 */
public class BadiffCli {

	public static void main(String[] args) {
		try {
			_main(args);
		} catch(Throwable t) {
			t.printStackTrace();
			System.exit(-1);
		}
		System.exit(0);
	}
	
	public static void _main(String[] args) throws Exception {
		if(args.length == 0) {
			help();
			return;
		}
		
		if("diff".equals(args[0])) {
			if(args.length != 4) {
				help();
				return;
			}
			BadiffFileDiff diff = new BadiffFileDiff(args[1]);
			File orig = new File(args[2]);
			File target = new File(args[3]);

			diff.diff(orig, target);
		}
		
		if("patch".equals(args[0])) {
			if(args.length != 4) {
				help();
				return;
			}
			BadiffFileDiff diff = new BadiffFileDiff(args[1]);
			File orig = new File(args[2]);
			File target = new File(args[3]);
			
			diff.apply(orig, target);
		}
		
		if("info".equals(args[0])) {
			if(args.length != 2) {
				help();
				return;
			}
			BadiffFileDiff diff = new BadiffFileDiff(args[1]);
			BadiffFileDiff.Header header = diff.header();
			Header.Stats stats = header.getStats();
			Header.Optional opt = header.getOptional();
			
			field("Input Size", stats.getInputSize());
			field("Output Size", stats.getOutputSize());
			field("Patch Size", diff.length());
			field("Inserts", stats.getInsertCount());
			field("Deletes", stats.getDeleteCount());
			field("Copies", stats.getNextCount());
			field("Rewinds", stats.getRewindCount());
			if(opt != null) {
				if(opt.getHashAlgorithm() != null)
					field("Hash", opt.getHashAlgorithm());
				if(opt.getPreHash() != null)
					field("Input Hash", Digests.pretty(opt.getPreHash()));
				if(opt.getPostHash() != null)
					field("Output Hash", Digests.pretty(opt.getPostHash()));
			}
		}
		
	}

	private static void field(String header, Object data) {
		header = header + "....................";
		header = header.substring(0, 20);
		System.out.println(header + data);
	}
	
	private static void help() {
		System.out.println("Command and options required:");
		System.out.println();
		System.out.println("diff DIFF_FILE ORIG_FILE TARGET_FILE");
		System.out.println("\tCompute the difference between ORIG_FILE and TARGET_FILE and store in DIFF_FILE");
		System.out.println();
		System.out.println("patch DIFF_FILE ORIG_FILE TARGET_FILE");
		System.out.println("\tApply a difference from DIFF_FILE to ORIG_FILE to generate TARGET_FILE");
		System.out.println();
		System.out.println("info DIFF_FILE");
		System.out.println("\tDisplay statistics and optional data from DIFF_FILE");
	}
}
