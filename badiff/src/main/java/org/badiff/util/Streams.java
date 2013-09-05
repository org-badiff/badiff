package org.badiff.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import org.badiff.io.ObjectInputInputStream;
import org.badiff.io.ObjectOutputOutputStream;

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
	
	public static long copy(InputStream in, OutputStream out, long length) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		for(int r = in.read(buf, 0, (int) Math.min(buf.length, length - count)); 
				r != -1; 
				r = in.read(buf, 0, (int) Math.min(buf.length, length - count))) {
			out.write(buf, 0, r);
			count += r;
		}
		return count;
	}
	
	public static OutputStream asStream(ObjectOutput out) {
		if(out instanceof OutputStream)
			return (OutputStream) out;
		return new ObjectOutputOutputStream(out);
	}
	
	public static InputStream asStream(ObjectInput in) {
		if(in instanceof InputStream)
			return (InputStream) in;
		return new ObjectInputInputStream(in);
	}
	
	private Streams() {}

}
