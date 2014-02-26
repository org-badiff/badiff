package org.badiff.exa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.badiff.Diff;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.imp.FileDiff;
import org.badiff.io.ByteBufferRandomInput;
import org.badiff.io.FileRandomInput;
import org.badiff.io.RandomInput;
import org.badiff.q.OpQueue;
import org.badiff.q.RandomChunkingOpQueue;
import org.badiff.q.StreamChunkingOpQueue;
import org.badiff.util.Diffs;

public class LargeFiles {

	public static void main(String[] args) throws Exception {
		File mm = new File("target/mm");
		mm.mkdirs();
		File origFile = new File(mm, "orig.bin");
		File targetFile = new File(mm, "target.bin");
		File diffFile = new File(mm, "diff.bin");
		File patchedFile = new File(mm, "patched.bin");
		long length = 10L * 1024 * 1024;

		System.out.println("Computing and applying diffs for random files of length:" + length);

		long start = System.currentTimeMillis();

		fill(origFile, length);
		distort(origFile, targetFile);

		long filled = System.currentTimeMillis();
		System.out.println("Fill time:" + TimeUnit.SECONDS.convert(filled - start, TimeUnit.MILLISECONDS) + "s");

		// mapping may take a moment
		InputStream orig = new FileInputStream(origFile);
		InputStream target = new FileInputStream(targetFile);

		OpQueue q = new StreamChunkingOpQueue(orig, target);
		// establishing a pipeline doesn't force a diff computation
		q = BadiffFileDiff.PIPE.from(q).outlet();

		Diff diff = new FileDiff(diffFile);
		diff.store(q);

		long diffCompleted = System.currentTimeMillis();
		System.out.println("Diff computation time:" + TimeUnit.SECONDS.convert(diffCompleted - filled, TimeUnit.MILLISECONDS) + "s");
		System.out.println("Diff size:" + diffFile.length());

		Diffs.apply(diff, origFile, patchedFile);

		long diffApplied = System.currentTimeMillis();
		System.out.println("Diff apply time:" + TimeUnit.SECONDS.convert(diffApplied - diffCompleted, TimeUnit.MILLISECONDS) + "s");
	}

	private static void fill(File file, long size) throws IOException {
		byte[] buf = new byte[1024];
		Random rand = new Random();
		OutputStream out = new FileOutputStream(file);
		try {
			while(size > 0) {
				rand.nextBytes(buf);
				int w = (int) Math.min(buf.length, size);
				out.write(buf, 0, w);
				size -= w;
			}
		} finally {
			out.close();
		}
	}

	private static void distort(File orig, File target) throws IOException {
		byte[] buf = new byte[12345];
		Random rand = new Random();
		InputStream in = new FileInputStream(orig);
		try {
			OutputStream out = new FileOutputStream(target);
			try {
				while(true) {
					int r = in.read(buf);
					if(r == -1)
						return;
					if(r < buf.length)
						buf = Arrays.copyOf(buf, r);
					for(int i = 0; i < rand.nextInt(7); i++) {
						int from = rand.nextInt(buf.length);
						int len = rand.nextInt(buf.length - from);
						if(rand.nextBoolean()) {
							byte[] rbuf = new byte[len];
							rand.nextBytes(rbuf);
							System.arraycopy(rbuf, 0, buf, from, len);
						} else {
							int to = rand.nextInt(buf.length - len);
							System.arraycopy(buf, from, buf, to, len);
						}
					}
					out.write(buf);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}
}
