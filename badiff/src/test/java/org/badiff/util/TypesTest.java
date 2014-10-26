package org.badiff.util;

import org.junit.Assert;
import org.junit.Test;

public class TypesTest {
	@Test
	public void testToWrapper() {
		for(int i = 0; i < Types.getPrimitiveCount(); i++) {
			Assert.assertEquals(Types.getWrapperTypes()[i], Types.toWrapper(Types.getPrimitiveTypes()[i]));
			Assert.assertEquals(Types.getWrapperTypes()[i], Types.toWrapper(Types.getWrapperTypes()[i]));
		}
		Assert.assertNull(Types.toWrapper(TypesTest.class));
	}

	@Test
	public void testToPrimitive() {
		for(int i = 0; i < Types.getPrimitiveCount(); i++) {
			Assert.assertEquals(Types.getPrimitiveTypes()[i], Types.toPrimitive(Types.getPrimitiveTypes()[i]));
			Assert.assertEquals(Types.getPrimitiveTypes()[i], Types.toPrimitive(Types.getWrapperTypes()[i]));
		}
		Assert.assertNull(Types.toPrimitive(TypesTest.class));
	}
	
	@Test
	public void testIsPrimitive() {
		for(int i = 0; i < Types.getPrimitiveCount(); i++) {
			Assert.assertTrue(Types.isPrimitive(Types.getPrimitiveTypes()[i]));
			Assert.assertFalse(Types.isPrimitive(Types.getWrapperTypes()[i]));
		}
	}
	
	@Test
	public void testIsWrapper() {
		for(int i = 0; i < Types.getPrimitiveCount(); i++) {
			Assert.assertFalse(Types.isWrapper(Types.getPrimitiveTypes()[i]));
			Assert.assertTrue(Types.isWrapper(Types.getWrapperTypes()[i]));
		}
	}
}
