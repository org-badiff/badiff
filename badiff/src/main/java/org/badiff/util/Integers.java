package org.badiff.util;

public class Integers {
	
	public static long any(long l) {
		l = l | (l << 32) | (l >>> 32);
		l = l | (l << 16) | (l >>> 16);
		l = l | (l << 8) | (l >>> 8);
		l = l | (l << 4) | (l >>> 4);
		l = l | (l << 2) | (l >>> 2);
		l = l | (l << 1) | (l >>> 1);
		return l;
	}
	
	public static int any(int i) {
		return (int) any((long) i);
	}
	
	public static short any(short s) {
		return (short) any((long) s);
	}
	
	public static long all(long l) {
		long c = Long.bitCount(l);
		return ~any(c ^ 64);
	}
	
	public static int all(int n) {
		long l = n;
		l = l | (l << 32);
		return (int) all(l);
	}
	
	public static short all(short s) {
		long l = s;
		l = l | (l << 32);
		l = l | (l << 16);
		return (short) all(l);
	}
	
	public static long cmp(long lhs, long rhs, long equal, long unequal) {
		long mask = any(lhs ^ rhs);
		return (equal & ~mask) | (unequal & mask);
	}
	
	public static int cmp(long lhs, long rhs, int equal, int unequal) {
		long mask = any(lhs ^ rhs);
		return (int)((equal & ~mask) | (unequal & mask));
	}
	
	public static short cmp(long lhs, long rhs, short equal, short unequal) {
		long mask = any(lhs ^ rhs);
		return (short)((equal & ~mask) | (unequal & mask));
	}
	
	private Integers() {}
}
