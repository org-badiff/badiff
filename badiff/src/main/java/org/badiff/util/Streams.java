package org.badiff.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import org.badiff.io.DataInputInputStream;
import org.badiff.io.DataOutputOutputStream;

/**
 * Utility methods for dealing with streams
 * @author robin
 *
 */
public class Streams {
	/**
	 * Copy all data from {@code in} to {@code out} without closing either
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		for(int r = in.read(buf); r != -1; r = in.read(buf)) {
			out.write(buf, 0, r);
			count += r;
		}
		return count;
	}
	
	/**
	 * Copy some data from {@code in} to {@code out} without closing either
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out, long length) throws IOException {
		long count = 0;
		byte[] buf = new byte[8192];
		for(int r = in.read(buf, 0, (int) Math.min(buf.length, length - count)); 
				r != -1; 
				r = in.read(buf, 0, (int) Math.min(buf.length, length - count))) {
			out.write(buf, 0, r);
			count += r;
			if(count == length)
				break;
		}
		return count;
	}
	
	/**
	 * View the {@link ObjectOutput} as an {@link OutputStream} by either casting or wrapping
	 * @param out
	 * @return
	 */
	public static OutputStream asStream(DataOutput out) {
		if(out instanceof OutputStream)
			return (OutputStream) out;
		return new DataOutputOutputStream(out);
	}
	
	/**
	 * View thw {@link ObjectInput} as an {@link InputStream} by either casting or wrapping
	 * @param in
	 * @return
	 */
	public static InputStream asStream(DataInput in) {
		if(in instanceof InputStream)
			return (InputStream) in;
		return new DataInputInputStream(in);
	}
	
	private Streams() {}

}
