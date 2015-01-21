package org.badiff.kryo;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class BadiffSerializerTest {
	@Test
	public void testSerialize() {
		String abc = "abcdefghijklmnopqrstuvwxyz";
		String def = "defghijklmnopqrstuvwxyzabc";
		
		Kryo kryo = new Kryo();
		kryo.register(String.class, new BadiffSerializer<String>(kryo));
		kryo.setAutoReset(false);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Output output = new Output(bytes);
		
		kryo.writeObject(output, abc);
		kryo.writeObject(output, def);
		
		output.close();
		
		kryo.reset();
		Input input = new Input(bytes.toByteArray());
				
		Assert.assertEquals(abc, kryo.readObject(input, String.class));
		Assert.assertEquals(def, kryo.readObject(input, String.class));
	}
	
	@Test
	public void testSerialize2() {
		String abc = "abcdefghijklmnopqrstuvwxyz";
		String def = "defghijklmnopqrstuvwxyzabc";
		
		Kryo kryo = new Kryo();
		kryo.register(String.class, new BadiffSerializer<String>(new Kryo()));
		kryo.setAutoReset(false);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Output output = new Output(bytes);
		
		kryo.writeObject(output, abc);
		kryo.writeObject(output, def);
		
		output.close();
		
		kryo.reset();
		Input input = new Input(bytes.toByteArray());
				
		Assert.assertEquals(abc, kryo.readObject(input, String.class));
		Assert.assertEquals(def, kryo.readObject(input, String.class));
	}
	
	public static interface I {}
	public static class C implements I {
		private String alpha;
		public C() {}
		
		public C(String alpha) {
			if(alpha == null)
				throw new IllegalArgumentException();
			this.alpha = alpha;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof C) {
				return alpha.equals(((C) obj).alpha);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			if(alpha == null)
				return 0;
			return alpha.hashCode();
		}
		
		@Override
		public String toString() {
			return alpha;
		}
	}
	
	@Test
	public void testDefaultSerialize() {
		C abc = new C("abcdefghijklmnopqrstuvwxyz");
		C def = new C("defghijklmnopqrstuvwxyzabc");
		
		Kryo kryo = new Kryo();
		kryo.addDefaultSerializer(I.class, BadiffSerializer.class);
		kryo.setAutoReset(false);
		
		Assert.assertTrue(kryo.getSerializer(C.class) instanceof BadiffSerializer<?>);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Output output = new Output(bytes);
		
		kryo.writeClassAndObject(output, abc);
		kryo.writeClassAndObject(output, def);
		
		output.close();
		
		kryo.reset();
		Input input = new Input(bytes.toByteArray());
				
		Assert.assertEquals(abc, kryo.readClassAndObject(input));
		Assert.assertEquals(def, kryo.readClassAndObject(input));
	}
}
