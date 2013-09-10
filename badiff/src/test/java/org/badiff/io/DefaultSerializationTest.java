package org.badiff.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class DefaultSerializationTest {
	@Test
	public void testLongs() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);
		
		DefaultSerialization.getInstance().writeLong(dout, 1);
		DefaultSerialization.getInstance().writeLong(dout, -1);
		DefaultSerialization.getInstance().writeLong(dout, 159);
		DefaultSerialization.getInstance().writeLong(dout, -159);
		DefaultSerialization.getInstance().writeLong(dout, Integer.MAX_VALUE + 1L);
		DefaultSerialization.getInstance().writeLong(dout, Integer.MIN_VALUE - 1L);
		
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
		Assert.assertEquals(1, DefaultSerialization.getInstance().readLong(din));
		Assert.assertEquals(-1, DefaultSerialization.getInstance().readLong(din));
		Assert.assertEquals(159, DefaultSerialization.getInstance().readLong(din));
		Assert.assertEquals(-159, DefaultSerialization.getInstance().readLong(din));
		Assert.assertEquals(Integer.MAX_VALUE + 1L, DefaultSerialization.getInstance().readLong(din));
		Assert.assertEquals(Integer.MIN_VALUE - 1L, DefaultSerialization.getInstance().readLong(din));
	}
}
