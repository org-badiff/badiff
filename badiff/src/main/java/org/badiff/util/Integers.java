package org.badiff.util;

public class Integers {
	
	public static long negative(long l) {
		return l >> 63;
	}
	
	public static int negative(int i) {
		return i >> 31;
	}
	
	public static short netative(short s) {
		return (short) negative((int) s);
	}
	
	public static long min(long lhs, long rhs) {
		long m = negative(lhs - rhs);
		return (lhs & m) | (rhs & ~m);
	}
	
	public static int min(int lhs, int rhs) {
		int m = negative(lhs - rhs);
		return (lhs & m) | (rhs & ~m);
	}
	
	public static short min(short lhs, short rhs) {
		return (short) min((int) lhs, (int) rhs);
	}
	
	public static long max(long lhs, long rhs) {
		long m = negative(lhs - rhs);
		return (lhs & ~m) | (rhs & m);
	}
	
	public static int max(int lhs, int rhs) {
		int m = negative(lhs - rhs);
		return (lhs & ~m) | (rhs & m);
	}
	
	public static short max(short lhs, short rhs) {
		return (short) max((int) lhs, (int) rhs);
	}
	
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
		long mask = negative(lhs - rhs) | negative(rhs - lhs);
		return (equal & ~mask) | (unequal & mask);
	}
	
	public static int cmp(long lhs, long rhs, int equal, int unequal) {
		long mask = negative(lhs - rhs) | negative(rhs - lhs);
		return (int)((equal & ~mask) | (unequal & mask));
	}
	
	public static short cmp(long lhs, long rhs, short equal, short unequal) {
		return (short) cmp(lhs, rhs, (int) equal, (int) unequal);
	}
	
	private Integers() {}
}
