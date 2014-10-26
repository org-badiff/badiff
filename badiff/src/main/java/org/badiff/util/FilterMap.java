package org.badiff.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class FilterMap<K, V> extends AbstractMap<K, V> {
	protected Map<K, V> wrapped;
	
	public FilterMap(Map<K, V> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return wrapped.entrySet();
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	@Override
	public V get(Object key) {
		return wrapped.get(key);
	}

	@Override
	public V put(K key, V value) {
		return wrapped.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return wrapped.remove(key);
	}

	@Override
	public void clear() {
		wrapped.clear();
	}
}
