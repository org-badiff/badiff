package org.badiff.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class FlatCollections {
	public static class FlatMap<K, V> extends AbstractMap<K, V> {
		protected static Object[][] entriesOf(Map<?, ?> m) {
			if(m.size() == 0)
				return new Object[2][0];
			Object[] keys = new Object[m.size()];
			Object[] vals = new Object[m.size()];
			int i = 0;
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Iterator<Map.Entry<?, ?>> ei = (Iterator) m.entrySet().iterator();
			for(Map.Entry<?, ?> e = ei.next(); ei.hasNext(); e = ei.next()) {
				keys[i] = e.getKey();
				vals[i] = e.getValue();
				i++;
			}
			return new Object[][] {keys, vals};
		}
		
		protected int size;
		protected int modulo;
		protected Object[] keys;
		protected Object[] values;
		protected int[] indexes;
		
		public FlatMap(Map<? extends K, ? extends V> m) {
			this(entriesOf(m));
		}
		
		@SuppressWarnings("unchecked")
		protected FlatMap(Object[][] entries) {
			this((K[]) entries[0], (V[]) entries[1]);
		}
		
		public FlatMap(K[] keys, V[] values) {
			if(keys.length != values.length)
				throw new IllegalArgumentException("keys and values arrays must have same length");
			size = keys.length;
			
			// Ensure no null keys
			// Ensure no duplicate keys
			for(int i = 0; i < size; i++) {
				if(keys[i] == null)
					throw new IllegalArgumentException("null keys are not permitted");
				for(int j = 0; j < size; j++)
					if(i != j && keys[i].equals(keys[j]))
						throw new IllegalArgumentException("Duplicate keys: " + keys[i] + " and " + keys[j]);
			}
			
			modulo = FlatCollections.arrayLength(keys);
			this.keys = new Object[modulo];
			this.values = new Object[modulo];
			indexes = new int[size];
			
			for(int i = 0; i < size; i++) {
				int idx = FlatCollections.scanKeyPosition(keys[i], this.keys);
				if(idx == -1)
					throw new IllegalStateException("No more room in keys array?");
				if(idx < 0)
					idx = -idx - 2;
				this.keys[idx] = keys[i];
				this.values[idx] = values[i];
				indexes[i] = idx;
			}
		}
		
		@Override
		public Set<Entry<K, V>> entrySet() {
			return new EntrySet();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean containsValue(Object value) {
			if(value == null)
				return false;
			for(int i = 0; i < modulo; i++)
				if(value.equals(values[i]))
					return true;
			return false;
		}

		@Override
		public boolean containsKey(Object key) {
			if(key == null)
				return false;
			int idx = FlatCollections.scanKeyPosition(key, keys);
			return idx >= 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public V get(Object key) {
			if(key == null)
				return null;
			int idx = FlatCollections.scanKeyPosition(key, keys);
			if(idx < 0)
				return null;
			return (V) values[idx];
		}

		@Override
		public V put(K key, V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V remove(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		protected class EntrySet extends AbstractSet<Map.Entry<K, V>> {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new EntryIterator();
			}
		
			@Override
			public int size() {
				return size;
			}
		}

		protected class EntryIterator implements Iterator<Map.Entry<K, V>> {
			protected int pos = 0;
		
			@Override
			public boolean hasNext() {
				return pos < size;
			}
		
			@Override
			public Map.Entry<K, V> next() {
				int idx = indexes[pos];
				@SuppressWarnings("unchecked")
				Map.Entry<K, V> e = new AbstractMap.SimpleImmutableEntry<K, V>((K) keys[idx], (V) values[idx]);
				pos++;
				return e;
			}
		
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}

	}

	public static class FlatSet<E> extends AbstractSet<E> {
		protected int size;
		protected int modulo;
		protected Object[] keys;
		protected int[] indexes;
		
		@SuppressWarnings("unchecked")
		public FlatSet(Collection<? extends E> c) {
			this((E[]) c.toArray());
		}
		
		public FlatSet(E[] keys) {
			size = keys.length;
			
			// Ensure no null keys
			// Ensure no duplicate keys
			for(int i = 0; i < size; i++) {
				if(keys[i] == null)
					throw new IllegalArgumentException("null keys are not permitted");
				for(int j = 0; j < size; j++)
					if(i != j && keys[i].equals(keys[j]))
						throw new IllegalArgumentException("Duplicate keys: " + keys[i] + " and " + keys[j]);
			}
			
			modulo = FlatCollections.arrayLength(keys);
			
			this.keys = new Object[modulo];
			indexes = new int[size];
			
			for(int i = 0; i < size; i++) {
				int idx = FlatCollections.scanKeyPosition(keys[i], this.keys);
				if(idx == -1)
					throw new IllegalStateException("No more room in keys array?");
				if(idx < 0)
					idx = -idx - 2;
				this.keys[idx] = keys[i];
				indexes[i] = idx;
			}
		}
		
		@Override
		public Iterator<E> iterator() {
			return new Itr();
		}

		@Override
		public int size() {
			return size;
		}


		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(Object o) {
			if(o == null)
				return false;
			int idx = FlatCollections.scanKeyPosition(o, keys);
			return idx >= 0;
		}

		@Override
		public boolean add(E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}


		protected class Itr implements Iterator<E> {
			protected int pos = 0;
		
			@Override
			public boolean hasNext() {
				return pos < size;
			}
		
			@Override
			public E next() {
				int idx = indexes[pos];
				@SuppressWarnings("unchecked")
				E e = (E) keys[idx];
				pos++;
				return e;
			}
		
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}

	}

	public static class InheritingFlatMap<K, V> extends FlatMap<K, V> {
		
		protected Map<K, V> parent;

		public InheritingFlatMap(Map<K, V> parent, K[] keys, V[] values) {
			super(keys, values);
			if(parent == null)
				parent = Collections.emptyMap();
			this.parent = parent;
		}

		public InheritingFlatMap(Map<K, V> parent, Map<? extends K, ? extends V> m) {
			this(parent, FlatMap.entriesOf(m));
		}

		@SuppressWarnings("unchecked")
		protected InheritingFlatMap(Map<K, V> parent, Object[][] entries) {
			this(parent, (K[]) entries[0], (V[]) entries[1]);
		}

		protected Set<Entry<K, V>> superEntrySet() {
			return super.entrySet();
		}
		
		@Override
		public Set<Entry<K, V>> entrySet() {
			return new EntrySet();
		}

		@Override
		public int size() {
			return entrySet().size();
		}

		@Override
		public boolean containsValue(Object value) {
			return values().contains(value);
		}
		
		protected boolean superContainsKey(Object key) {
			return super.containsKey(key);
		}

		@Override
		public boolean containsKey(Object key) {
			return super.containsKey(key) || parent.containsKey(key);
		}

		@Override
		public V get(Object key) {
			if(super.containsKey(key))
				return super.get(key);
			return parent.get(key);
		}

		@Override
		public boolean isEmpty() {
			return super.isEmpty() && parent.isEmpty();
		}

		protected class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		
			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new EntryIterator();
			}
		
			@Override
			public int size() {
				int size = 0;
				for(@SuppressWarnings("unused") Object e : this)
					size++;
				return size;
			}
		}

		protected class EntryIterator implements Iterator<Map.Entry<K, V>> {
			private Iterator<Map.Entry<K, V>> superItr = superEntrySet().iterator();   
			private Iterator<Map.Entry<K, V>> parentItr = parent.entrySet().iterator();
			
			private Map.Entry<K, V> next;
	
			@Override
			public boolean hasNext() {
				if(superItr.hasNext() || next != null)
					return true;
				while(parentItr.hasNext()) {
					Map.Entry<K, V> e = parentItr.next();
					if(superContainsKey(e.getKey()))
						continue;
					next = e;
					return true;
				}
				return false;
			}
	
			@Override
			public java.util.Map.Entry<K, V> next() {
				if(!hasNext())
					throw new NoSuchElementException();
				if(superItr.hasNext())
					return superItr.next();
				Map.Entry<K, V> e = next;
				next = null;
				return e;
			}
	
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		}
	}
	
	public static int primaryIndexOf(Object key, int modulo) {
		return Math.abs(key.hashCode()) % modulo;
	}
	
	public static int scanKeyPosition(Object key, Object[] keys) {
		if(keys.length == 0)
			return -1;
		
		int start = primaryIndexOf(key, keys.length);
		int idx = start;
		
		do {
			if(keys[idx] == null)
				return -idx - 2;
			if(key.equals(keys[idx]))
				return idx;
			idx = (idx + 1) % keys.length;
		} while(idx != start);
		
		return -1;
	}
	
	public static int arrayLength(Object[] objects) {
		int[] hashCodes = new int[objects.length];
		for(int i = 0; i < objects.length; i++)
			hashCodes[i] = Math.abs(objects[i].hashCode());
		Set<Integer> mods = new HashSet<Integer>();
		findModulo: for(int modulo = objects.length; ; modulo++) {
			mods.clear();
			for(int i = 0; i < objects.length; i++)
				if(!mods.add(hashCodes[i] % modulo))
					continue findModulo;
			return Math.max(modulo, objects.length);
		}
	}

	
	private FlatCollections() {}
}
