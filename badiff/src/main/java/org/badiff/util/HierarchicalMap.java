package org.badiff.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class HierarchicalMap<K, V> extends FilterMap<K, V> {
	protected Map<K, V> parent;
	protected Set<K> keys;
	
	public HierarchicalMap(Map<K, V> wrapped, Map<K, V> parent) {
		super(wrapped);
		this.parent = parent;
		keys = new HierarchicalSet<K>(wrapped.keySet(), parent.keySet());
	}

	protected Set<Map.Entry<K, V>> wrappedEntrySet() {
		return wrapped.entrySet();
	}
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	@Override
	public int size() {
		return keys.size();
	}

	@Override
	public boolean isEmpty() {
		return keys.isEmpty();
	}

	@Override
	public V get(Object key) {
		return wrapped.containsKey(key) ? wrapped.get(key) : parent.get(key);
	}

	@Override
	public V put(K key, V value) {
		V old = get(key);
		super.put(key, value);
		return old;
	}

	@Override
	public V remove(Object key) {
		return wrapped.remove(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return keys.contains(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		Iterator<Map.Entry<K, V>> ei = entrySet().iterator();
		while(ei.hasNext()) {
			V val = ei.next().getValue();
			if(value == null ? val == null : value.equals(val))
				return true;
		}
		return false;
	}

	protected Set<K> wrappedKeySet() {
		return wrapped.keySet();
	}
	
	@Override
	public Set<K> keySet() {
		return keys;
	}
	
	
	
	protected class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public int size() {
			return keys.size();
		}
		
	}
	
	protected class EntryIterator implements Iterator<Map.Entry<K, V>> {
		protected Iterator<Map.Entry<K, V>> wrappedItr = wrappedEntrySet().iterator();
		protected Iterator<Map.Entry<K, V>> parentItr = parent.entrySet().iterator();
		private Map.Entry<K, V> next;
		private boolean removable = true;
	
		@Override
		public boolean hasNext() {
			if(wrappedItr.hasNext() || next != null)
				return true;
			while(parentItr.hasNext()) {
				Map.Entry<K, V> e = parentItr.next();
				if(wrappedKeySet().contains(e.getKey()))
					continue;
				next = e;
				return true;
			}
			return false;
		}
	
		@Override
		public Map.Entry<K, V> next() {
			if(!hasNext())
				throw new NoSuchElementException();
			if(wrappedItr.hasNext())
				return wrappedItr.next();
			removable = false;
			Map.Entry<K, V> e = next;
			next = null;
			return e;
		}
	
		@Override
		public void remove() {
			if(removable)
				wrappedItr.remove();
			else
				throw new UnsupportedOperationException();
		}
	}
}
