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
		
		DefaultSerialization serial = DefaultSerialization.newInstance();
		
		serial.writeLong(dout, 1);
		serial.writeLong(dout, -1);
		serial.writeLong(dout, 159);
		serial.writeLong(dout, -159);
		serial.writeLong(dout, Integer.MAX_VALUE + 1L);
		serial.writeLong(dout, Integer.MIN_VALUE - 1L);
		
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
		Assert.assertEquals(1, serial.readLong(din));
		Assert.assertEquals(-1, serial.readLong(din));
		Assert.assertEquals(159, serial.readLong(din));
		Assert.assertEquals(-159, serial.readLong(din));
		Assert.assertEquals(Integer.MAX_VALUE + 1L, serial.readLong(din));
		Assert.assertEquals(Integer.MIN_VALUE - 1L, serial.readLong(din));
	}
}
