package org.badiff.util;

/**
 * Integer math routines that can compute conditional values
 * with no branching.  For tight loops, use of these routines
 * can result in a massive performance increase, because
 * the CPU never makes an incorrect branch prediction.
 * 
 * @author Robin
 *
 */
public class Integers {
	
	/**
	 * Return a mask for whether the argument is negative.
	 * @param l The number to check
	 * @return {@code -1} if negative, {@code 0} if non-negative
	 */
	public static long negative(long l) {
		return l >> 63;
	}
	
	/**
	 * Return a mask for whether the argument is negative.
	 * @param i The number to check
	 * @return {@code -1} if negative, {@code 0} if non-negative
	 */
	public static int negative(int i) {
		return i >> 31;
	}
	
	/**
	 * Return a mask for whether the argument is negative.
	 * @param s The number to check
	 * @return {@code -1} if negative, {@code 0} if non-negative
	 */
	public static short netative(short s) {
		return (short) negative((int) s);
	}
	
	/**
	 * Returns the minimum of the two arguments.  Equivalent
	 * to {@code (lhs < rhs ? lhs : rhs)} but requires no
	 * branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @return The minimum of {@code lhs} and {@code rhs}
	 */
	public static long min(long lhs, long rhs) {
		long m = negative(lhs - rhs);
		return (lhs & m) | (rhs & ~m);
	}
	
	/**
	 * Returns the minimum of the two arguments.  Equivalent
	 * to {@code (lhs < rhs ? lhs : rhs)} but requires no
	 * branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @return The minimum of {@code lhs} and {@code rhs}
	 */
	public static int min(int lhs, int rhs) {
		int m = negative(lhs - rhs);
		return (lhs & m) | (rhs & ~m);
	}
	
	/**
	 * Returns the minimum of the two arguments.  Equivalent
	 * to {@code (lhs < rhs ? lhs : rhs)} but requires no
	 * branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @return The minimum of {@code lhs} and {@code rhs}
	 */
	public static short min(short lhs, short rhs) {
		return (short) min((int) lhs, (int) rhs);
	}
	
	/**
	 * Returns the maximum of the two arguments.  Equivalent
	 * to {@code (lhs > rhs ? lhs : rhs)} but requires no
	 * branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @return The maximum of {@code lhs} and {@code rhs}
	 */
	public static long max(long lhs, long rhs) {
		long m = negative(lhs - rhs);
		return (lhs & ~m) | (rhs & m);
	}
	
	/**
	 * Returns the maximum of the two arguments.  Equivalent
	 * to {@code (lhs > rhs ? lhs : rhs)} but requires no
	 * branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @return The maximum of {@code lhs} and {@code rhs}
	 */
	public static int max(int lhs, int rhs) {
		int m = negative(lhs - rhs);
		return (lhs & ~m) | (rhs & m);
	}
	
	/**
	 * Returns the maximum of the two arguments.  Equivalent
	 * to {@code (lhs > rhs ? lhs : rhs)} but requires no
	 * branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @return The maximum of {@code lhs} and {@code rhs}
	 */
	public static short max(short lhs, short rhs) {
		return (short) max((int) lhs, (int) rhs);
	}
	
	/**
	 * Returns a mask for whether any bits of the argument are set.
	 * Equivalent to {@code (l == 0 ? 0 : -1)} but requires
	 * no branching.
	 * @param l The argument to check
	 * @return {@code -1} if any bits are set, {@code 0} otherwise.
	 */
	public static long any(long l) {
		l = l | (l << 32) | (l >>> 32);
		l = l | (l << 16) | (l >>> 16);
		l = l | (l << 8) | (l >>> 8);
		l = l | (l << 4) | (l >>> 4);
		l = l | (l << 2) | (l >>> 2);
		l = l | (l << 1) | (l >>> 1);
		return l;
	}
	
	/**
	 * Returns a mask for whether any bits of the argument are set.
	 * Equivalent to {@code (l == 0 ? 0 : -1)} but requires
	 * no branching.
	 * @param l The argument to check
	 * @return {@code -1} if any bits are set, {@code 0} otherwise.
	 */
	public static int any(int i) {
		return (int) any((long) i);
	}
	
	/**
	 * Returns a mask for whether any bits of the argument are set.
	 * Equivalent to {@code (l == 0 ? 0 : -1)} but requires
	 * no branching.
	 * @param l The argument to check
	 * @return {@code -1} if any bits are set, {@code 0} otherwise.
	 */
	public static short any(short s) {
		return (short) any((long) s);
	}
	
	/**
	 * Returns a mask for whether all bits of the argument are set.
	 * Equivalent to {@code (l == -1 ? -1 : 0)} but requires
	 * no branching.
	 * @param l The argument to check
	 * @return {@code -1} if all bits are set, {@code 0} otherwise.
	 */
	public static long all(long l) {
		long c = Long.bitCount(l);
		return ~any(c ^ 64);
	}
	
	/**
	 * Returns a mask for whether all bits of the argument are set.
	 * Equivalent to {@code (l == -1 ? -1 : 0)} but requires
	 * no branching.
	 * @param l The argument to check
	 * @return {@code -1} if all bits are set, {@code 0} otherwise.
	 */
	public static int all(int n) {
		long l = n;
		l = l | (l << 32);
		return (int) all(l);
	}
	
	/**
	 * Returns a mask for whether all bits of the argument are set.
	 * Equivalent to {@code (l == -1 ? -1 : 0)} but requires
	 * no branching.
	 * @param l The argument to check
	 * @return {@code -1} if all bits are set, {@code 0} otherwise.
	 */
	public static short all(short s) {
		long l = s;
		l = l | (l << 32);
		l = l | (l << 16);
		return (short) all(l);
	}
	
	/**
	 * Compares whether {@code lhs} and {@code rhs} are equal, and
	 * returns either {@code equal} or {@code unequal}.  Equivalent
	 * to {@code (lhs == rhs ? equal : unequal)} but requires
	 * no branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @param equal Returned if {@code lhs == rhs}
	 * @param unequal Returned if {@code lhs != rhs}
	 * @return {@code (lhs == rhs ? equal : unequal)}
	 */
	public static long cmp(long lhs, long rhs, long equal, long unequal) {
		long mask = negative(lhs - rhs) | negative(rhs - lhs);
		return (equal & ~mask) | (unequal & mask);
	}
	
	/**
	 * Compares whether {@code lhs} and {@code rhs} are equal, and
	 * returns either {@code equal} or {@code unequal}.  Equivalent
	 * to {@code (lhs == rhs ? equal : unequal)} but requires
	 * no branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @param equal Returned if {@code lhs == rhs}
	 * @param unequal Returned if {@code lhs != rhs}
	 * @return {@code (lhs == rhs ? equal : unequal)}
	 */
	public static int cmp(long lhs, long rhs, int equal, int unequal) {
		long mask = negative(lhs - rhs) | negative(rhs - lhs);
		return (int)((equal & ~mask) | (unequal & mask));
	}
	
	/**
	 * Compares whether {@code lhs} and {@code rhs} are equal, and
	 * returns either {@code equal} or {@code unequal}.  Equivalent
	 * to {@code (lhs == rhs ? equal : unequal)} but requires
	 * no branching.
	 * @param lhs The left-hand side
	 * @param rhs The right-hand side
	 * @param equal Returned if {@code lhs == rhs}
	 * @param unequal Returned if {@code lhs != rhs}
	 * @return {@code (lhs == rhs ? equal : unequal)}
	 */
	public static short cmp(long lhs, long rhs, short equal, short unequal) {
		return (short) cmp(lhs, rhs, (int) equal, (int) unequal);
	}
	
	private Integers() {}
}
