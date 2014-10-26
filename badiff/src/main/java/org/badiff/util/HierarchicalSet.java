package org.badiff.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class HierarchicalSet<E> extends FilterSet<E> {
	protected Set<? extends E> parent;
	
	public HierarchicalSet(Set<E> wrapped, Set<? extends E> parent) {
		super(wrapped);
		this.parent = parent;
	}
	
	protected Iterator<E> superIterator() {
		return super.iterator();
	}

	@Override
	public Iterator<E> iterator() {
		return new HierarchicalSetIterator();
	}

	@Override
	public int size() {
		int size = super.size();
		for(E e : parent)
			if(!super.contains(e))
				size++;
		return size;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		super.removeAll(c);
		return !containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && parent.isEmpty();
	}
	
	protected boolean superContains(Object o) {
		return super.contains(o);
	}

	@Override
	public boolean contains(Object o) {
		return super.contains(o) || parent.contains(o);
	}

	@Override
	public boolean add(E e) {
		return super.add(e) && !parent.contains(e);
	}

	@Override
	public boolean remove(Object o) {
		return super.remove(o) && !parent.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object e : c)
			if(!contains(e))
				return false;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return super.addAll(c) && !parent.containsAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		int oldSize = size();
		return super.retainAll(c) && oldSize != size();
	}

	@Override
	public void clear() {
		super.clear();
	}

	private class HierarchicalSetIterator implements Iterator<E> {
		private Iterator<E> superItr = superIterator();
		private Iterator<? extends E> parentItr = parent.iterator();
		private E next;
	
		@Override
		public boolean hasNext() {
			if(superItr.hasNext() || next != null)
				return true;
			while(parentItr.hasNext()) {
				E e = parentItr.next();
				if(superContains(e))
					continue;
				next = e;
				return true;
			}
			return false;
		}
	
		@Override
		public E next() {
			if(!hasNext())
				throw new NoSuchElementException();
			if(superItr.hasNext())
				return superItr.next();
			E e = next;
			next = null;
			return e;
		}
	
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
