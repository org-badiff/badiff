package org.badiff.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class FilterSet<E> extends AbstractSet<E> {
	protected Set<E> wrapped;
	
	public FilterSet(Set<E> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Iterator<E> iterator() {
		return wrapped.iterator();
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return wrapped.removeAll(c);
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return wrapped.contains(o);
	}

	@Override
	public boolean add(E e) {
		return wrapped.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return wrapped.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wrapped.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return wrapped.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return wrapped.retainAll(c);
	}

	@Override
	public void clear() {
		wrapped.clear();
	}
}
