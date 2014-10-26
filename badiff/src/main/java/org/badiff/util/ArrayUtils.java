package org.badiff.util;

public abstract class ArrayUtils {

	public static <E> E[] append(E[] lhs, E... rhs) {
		E[] es = java.util.Arrays.copyOf(lhs, lhs.length + rhs.length);
		System.arraycopy(rhs, 0, es, lhs.length, rhs.length);
		return es;
	}
	
	private ArrayUtils() {}
}
