package org.badiff.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {

	public static long copy(InputStream in, OutputStream out) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		for(int r = in.read(buf); r != -1; r = in.read(buf)) {
			out.write(buf, 0, r);
			count += r;
		}
		return count;
	}
	
	private Streams() {}

}
