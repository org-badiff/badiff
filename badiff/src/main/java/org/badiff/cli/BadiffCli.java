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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.badiff.Diff;
import org.badiff.fmt.BadiffFormat;
import org.badiff.fmt.OutputFormat;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.imp.BadiffFileDiff.Header;
import org.badiff.imp.FileDiff;
import org.badiff.imp.StreamQueueable;
import org.badiff.imp.StreamStoreable;
import org.badiff.io.FileRandomInput;
import org.badiff.io.RandomInput;
import org.badiff.io.RandomInputStream;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.RewindingOpQueue;
import org.badiff.q.UndoOpQueue;
import org.badiff.util.Diffs;

public class BadiffCli {

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			help();
			return;
		}
		
		if("diff".equals(args[0])) {
			if(args.length != 4) {
				help();
				return;
			}
			File orig = new File(args[1]);
			File target = new File(args[2]);
			File diff = new File(args[3]);
			
			RandomInput ori = new FileRandomInput(orig);
			RandomInputStream oin = new RandomInputStream(ori);
			
			OpQueue q = Diffs.queue(oin, new RandomInputStream(target));
			q = Diffs.improved(q);
			q = new RewindingOpQueue(q);
			FileDiff tmp = new FileDiff(new File(diff.getParentFile(), diff.getName() + ".tmp"));
			tmp.store(q);
			
			OutputFormat fmt = new BadiffFormat();
			DataOutputStream out = new DataOutputStream(new FileOutputStream(diff));
			ori.seek(0);
			fmt.exportDiff(tmp, ori, out);
			out.close();
			ori.close();
			
			tmp.delete();
		}
		
		if("info".equals(args[0])) {
			if(args.length != 2) {
				help();
				return;
			}
			BadiffFileDiff diff = new BadiffFileDiff(args[1]);
			BadiffFileDiff.Header header = diff.header();
			BadiffFileDiff.Stats stats = header.getStats();
			BadiffFileDiff.Optional opt = header.getOptional();
			System.out.println("Inserts:" + stats.getInsertCount());
			System.out.println("Deletes:" + stats.getDeleteCount());
			if(opt != null) {
				if(opt.getPreHash() != null) {
					System.out.println("Hash algorithm:" + opt.getHashAlgorithm());
					System.out.println("Pre-hash:" + Arrays.toString(opt.getPreHash()));
					System.out.println("Post-hash:" + Arrays.toString(opt.getPostHash()));
				}
			}
		}
		
	}

	
	private static void help() {
		System.out.println("Command and options required:");

	}
}
