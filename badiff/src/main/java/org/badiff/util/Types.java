package org.badiff.util;

import java.util.Arrays;
import java.util.Map;
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

	private static final Set<Class<?>> WRAPPER_TYPES_SET =
			new HashArrays.HashArraySet<Class<?>>(WRAPPER_TYPES);
	
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER =
			new HashArrays.HashArrayMap<Class<?>, Class<?>>(PRIMITIVE_TYPES, WRAPPER_TYPES);
	
	private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE =
			new HashArrays.HashArrayMap<Class<?>, Class<?>>(WRAPPER_TYPES, PRIMITIVE_TYPES);
	
	public static Class<?> toPrimitive(Class<?> type) {
		if(isPrimitive(type))
			return type;
		return WRAPPER_TO_PRIMITIVE.get(type);
	}
	
	public static Class<?> toWrapper(Class<?> type) {
		if(isWrapper(type))
			return type;
		return PRIMITIVE_TO_WRAPPER.get(type);
	}
	
	public static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive();
	}
	
	public static boolean isWrapper(Class<?> type) {
		return WRAPPER_TYPES_SET.contains(type);
	}
	
	public static boolean isPrimitiveOrWrapper(Class<?> type) {
		return isPrimitive(type) || isWrapper(type);
	}
	
	public static Class<?>[] getPrimitiveTypes() {
		return Arrays.copyOf(PRIMITIVE_TYPES, PRIMITIVE_TYPES.length);
	}

	public static Class<?>[] getWrapperTypes() {
		return Arrays.copyOf(WRAPPER_TYPES, WRAPPER_TYPES.length);
	}

	public static int getPrimitiveCount() {
		return PRIMITIVE_COUNT;
	}

	private Types() {}
}
