package org.badiff.patcher.util;

import java.util.HashSet;
import java.util.Set;

public abstract class Sets {
	
	public static <T> Set<T> union(Set<? extends T> lhs, Set<? extends T> rhs) {
		Set<T> u = new HashSet<T>(lhs);
		u.addAll(rhs);
		return u;
	}
	
	public static <T> Set<T> intersection(Set<? extends T> lhs, Set<? extends T> rhs) {
		Set<T> i = new HashSet<T>(lhs);
		i.retainAll(rhs);
		return i;
	}
	
	public static <T> Set<T> subtraction(Set<? extends T> lhs, Set<? extends T> rhs) {
		Set<T> s = new HashSet<T>(lhs);
		lhs.removeAll(rhs);
		return s;
	}
	
	public static <T> Set<T> disjoint(Set<? extends T> lhs, Set<? extends T> rhs) {
		return subtraction(union(lhs, rhs), intersection(lhs, rhs));
	}

	private Sets() {}
}
