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

	public Set<Map.Entry<K, V>> superEntrySet() {
		return super.entrySet();
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
		return super.containsKey(key) ? super.get(key) : parent.get(key);
	}

	@Override
	public V put(K key, V value) {
		V old = get(key);
		super.put(key, value);
		return old;
	}

	@Override
	public V remove(Object key) {
		return super.remove(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return keys.contains(key);
	}

	public Set<K> superKeySet() {
		return super.keySet();
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
		protected Iterator<Map.Entry<K, V>> superItr = superEntrySet().iterator();
		protected Iterator<Map.Entry<K, V>> parentItr = parent.entrySet().iterator();
		private Map.Entry<K, V> next;
		private boolean removable = true;
	
		@Override
		public boolean hasNext() {
			if(superItr.hasNext() || next != null)
				return true;
			while(parentItr.hasNext()) {
				Map.Entry<K, V> e = parentItr.next();
				if(superKeySet().contains(e.getKey()))
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
			if(superItr.hasNext())
				return superItr.next();
			removable = false;
			Map.Entry<K, V> e = next;
			next = null;
			return e;
		}
	
		@Override
		public void remove() {
			if(removable)
				superItr.remove();
			else
				throw new UnsupportedOperationException();
		}
	}
}
