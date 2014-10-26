package org.badiff.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Types {
	

	private static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[] {
		boolean.class,
		byte.class,
		char.class,
		double.class,
		float.class,
		int.class,
		long.class,
		short.class
	};
	
	private static final Class<?>[] WRAPPER_TYPES = new Class<?>[] {
		Boolean.class,
		Byte.class,
		Character.class,
		Double.class,
		Float.class,
		Integer.class,
		Long.class,
		Short.class
	};

	private static final int PRIMITIVE_COUNT = PRIMITIVE_TYPES.length;
	
	private static final Class<?>[] PRIMITIVE_TO_WRAPPER = new Class<?>[moduloHash(PRIMITIVE_TYPES)];
	private static final Class<?>[] WRAPPER_TO_PRIMITIVE = new Class<?>[moduloHash(WRAPPER_TYPES)];
	private static final Class<?>[] WRAPPER_TO_WRAPPER = new Class<?>[moduloHash(WRAPPER_TYPES)];
	
	static {
		for(int i = 0; i < PRIMITIVE_COUNT; i++) {
			Class<?> primitive = PRIMITIVE_TYPES[i];
			Class<?> wrapper = WRAPPER_TYPES[i];
			PRIMITIVE_TO_WRAPPER[Math.abs(primitive.hashCode()) % PRIMITIVE_TO_WRAPPER.length] = wrapper;
			WRAPPER_TO_PRIMITIVE[Math.abs(wrapper.hashCode()) % WRAPPER_TO_PRIMITIVE.length] = primitive;
			WRAPPER_TO_WRAPPER[Math.abs(wrapper.hashCode()) % WRAPPER_TO_WRAPPER.length] = wrapper;
		}
	}
	
	private static int moduloHash(Class<?>[] types) {
		int[] hashCodes = new int[types.length];
		for(int i = 0; i < types.length; i++)
			hashCodes[i] = Math.abs(types[i].hashCode());
		Set<Integer> mods = new HashSet<Integer>();
		findModulo: for(int modulo = types.length; ; modulo++) {
			mods.clear();
			for(int i = 0; i < types.length; i++)
				if(!mods.add(hashCodes[i] % modulo))
					continue findModulo;
			return modulo;
		}
	}
	
	public static Class<?> toPrimitive(Class<?> type) {
		if(type.isPrimitive())
			return type;
		int hc = Math.abs(type.hashCode());
		if(WRAPPER_TO_WRAPPER[hc % WRAPPER_TO_WRAPPER.length] != type)
			return null;
		return WRAPPER_TO_PRIMITIVE[hc % WRAPPER_TO_PRIMITIVE.length];
	}
	
	public static Class<?> toWrapper(Class<?> type) {
		int hc = Math.abs(type.hashCode());
		if(type.isPrimitive())
			return PRIMITIVE_TO_WRAPPER[hc % PRIMITIVE_TO_WRAPPER.length];
		if(WRAPPER_TO_WRAPPER[hc % WRAPPER_TO_WRAPPER.length] == type)
			return type;
		return null;
	}
	
	public static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive();
	}
	
	public static boolean isWrapper(Class<?> type) {
		int hc = Math.abs(type.hashCode());
		return WRAPPER_TO_WRAPPER[hc % WRAPPER_TO_WRAPPER.length] == type;
	}
	
	public static boolean isPrimitiveOrWrapper(Class<?> type) {
		return isPrimitive(type) || isWrapper(type);
	}
	
	private Types() {}

	public static Class<?>[] getPrimitiveTypes() {
		return Arrays.copyOf(PRIMITIVE_TYPES, PRIMITIVE_TYPES.length);
	}

	public static Class<?>[] getWrapperTypes() {
		return Arrays.copyOf(WRAPPER_TYPES, WRAPPER_TYPES.length);
	}

	public static int getPrimitiveCount() {
		return PRIMITIVE_COUNT;
	}
}
