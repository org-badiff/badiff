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
package org.badiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.badiff.imp.FileDiff;
import org.badiff.io.RandomInputStream;
import org.badiff.q.OneWayOpQueue;
import org.badiff.q.StreamChunkingOpQueue;
import org.badiff.q.UndoOpQueue;
import org.badiff.util.Diffs;

/**
 * Utilities for dealing with {@link Diff}s as {@link File}
 * @author robin
 *
 */
public class FileDiffs {
	
	/**
	 * Compute and return a diff between {@code orig} and {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public static FileDiff diff(File orig, File target) throws IOException {
		FileDiff fd = new FileDiff(File.createTempFile(orig.getName(), ".diff"));
		InputStream oin = new FileInputStream(orig);
		try {
			InputStream tin = new FileInputStream(target);
			try {
				fd.store(Diffs.improved(Diffs.queue(oin, tin)));
			} finally {
				tin.close();
			}
		} finally {
			oin.close();
		}
		return fd;
	}
	
	/**
	 * Compute and return a diff between {@code orig} and {@code target} using
	 * memory-mapped files
	 * @param orig
	 * @param target
	 * @return
	 * @throws IOException
	 */
	public static FileDiff mdiff(File orig, File target) throws IOException {
		FileDiff fd = new FileDiff(File.createTempFile(orig.getName(), ".diff"));
		RandomInputStream oin = new RandomInputStream(orig);
		try {
			RandomInputStream tin = new RandomInputStream(target);
			try {
				fd.store(Diffs.improved(new StreamChunkingOpQueue(oin, tin)));
			} finally {
				tin.close();
			}
		} finally {
			oin.close();
		}
		return fd;
	}
	
	/**
	 * Apply {@code diff} to {@code orig} and return the result
	 * @param orig
	 * @param diff
	 * @return
	 */
	public static File apply(File orig, Diff diff) throws IOException {
		File target = File.createTempFile(orig.getName(), ".target");
		Diffs.apply(diff, orig, target);
		return target;
	}
	
	/**
	 * Compute and return a one-way (unidirectional) diff from {@code orig} to {@code target}
	 * @param orig
	 * @param target
	 * @return
	 */
	public static FileDiff udiff(File orig, File target) throws IOException {
		FileDiff fd = new FileDiff(File.createTempFile(orig.getName(), ".udiff"));
		InputStream oin = new FileInputStream(orig);
		try {
			InputStream tin = new FileInputStream(target);
			try {
				fd.store(new OneWayOpQueue(Diffs.improved(Diffs.queue(oin, tin))));
			} finally {
				tin.close();
			}
		} finally {
			oin.close();
		}
		return fd;
	}
	
	/**
	 * Apply the inverse of {@code diff} to {@code target} and return the result
	 * @param target
	 * @param diff
	 * @return
	 */
	public static File undo(File target, Diff diff) throws IOException {
		File orig = File.createTempFile(target.getName(), ".orig");
		Diffs.apply(new UndoOpQueue(diff.queue()), target, orig);
		return orig;
	}
	
	/**
	 * Compute and return a one-way (unidirectional) diff given any diff
	 * @param diff
	 * @return
	 */
	public static FileDiff udiff(Diff diff) throws IOException {
		FileDiff ud = new FileDiff(File.createTempFile("udiff", ".udiff"));
		ud.store(new OneWayOpQueue(diff.queue()));
		return ud;
	}
	
	/**
	 * Compute and return an "undo" diff from a two-way diff
	 * @param diff
	 * @return
	 */
	public static FileDiff undo(Diff diff) throws IOException {
		FileDiff ud = new FileDiff(File.createTempFile("undo", ".undo"));
		ud.store(new UndoOpQueue(diff.queue()));
		return ud;
	}
	
	private FileDiffs() {}
}
