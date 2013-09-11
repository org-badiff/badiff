package org.badiff.cli;

import java.io.FileInputStream;

import org.badiff.imp.FileDiff;
import org.badiff.imp.StreamQueueable;
import org.badiff.imp.StreamStoreable;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.q.UndoOpQueue;
import org.badiff.util.Diffs;

public class BadiffCli {

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			help();
			return;
		}
		if("diff".equals(args[0])) {
			if(args.length != 3) {
				help();
				return;
			}
			FileInputStream orig = new FileInputStream(args[1]);
			FileInputStream target = new FileInputStream(args[2]);
			OpQueue q = Diffs.queue(orig, target);
			q = Diffs.improved(q);
			new StreamStoreable(System.out).store(q);
			return;
		}
		
		if("udiff".equals(args[0])) {
			if(args.length != 3) {
				help();
				return;
			}
			FileInputStream orig = new FileInputStream(args[1]);
			FileInputStream target = new FileInputStream(args[2]);
			OpQueue q = Diffs.queue(orig, target);
			q = Diffs.improved(q);
			q = new OneWayOpQueue(q);
			new StreamStoreable(System.out).store(q);
			return;
		}
		
		if("patch".equals(args[0])) {
			if(args.length != 3) {
				help();
				return;
			}
			FileInputStream orig = new FileInputStream(args[1]);
			FileDiff diff = new FileDiff(args[2]);
			diff.apply(orig, System.out);
			return;
		}

		if("unpatch".equals(args[0])) {
			if(args.length != 3) {
				help();
				return;
			}
			FileInputStream patched = new FileInputStream(args[1]);
			FileDiff diff = new FileDiff(args[2]);
			OpQueue q = diff.queue();
			q = new UndoOpQueue(q);
			q.apply(patched, System.out);
			return;
		}
		
		if("strip".equals(args[0])) {
			if(args.length != 1) {
				help();
				return;
			}
			OpQueue q = new StreamQueueable(System.in).queue();
			q = new OneWayOpQueue(q);
			new StreamStoreable(System.out).store(q);
		}
}

	
	private static void help() {
		System.out.println("Command and options required:");

		System.out.println("badiff diff ORIG TARGET");
		System.out.println("\tWrite a bi-directional diff to standard out");
		
		System.out.println("badiff udiff ORIG TARGET");
		System.out.println("\tWrite a one-way diff to standard out");
		
		System.out.println("badiff patch ORIG DIFF");
		System.out.println("\tWrite the patched file to standard out");
		
		System.out.println("badiff unpatch PATCHED DIFF");
		System.out.println("\tWrite the original file to standard out, given a bi-directional diff");
		
		System.out.println("badiff strip");
		System.out.println("\tRead an any-directional diff from standard in and write a one-way diff to standard out");
	}
}
