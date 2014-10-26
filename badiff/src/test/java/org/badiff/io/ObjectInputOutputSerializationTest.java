package org.badiff.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import org.badiff.Op;
import org.junit.Assert;
import org.junit.Test;

public class ObjectInputOutputSerializationTest {
	@Test
	public void testSerialization() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		ObjectOutput oo = new ObjectOutputStream(buf);
		ObjectInputOutputSerialization serial = new ObjectInputOutputSerialization();
		
		Op op = new Op(Op.INSERT, 5, "Hello".getBytes(Charset.forName("ASCII")));
		serial.writeObject(oo, Op.class, op);
		oo.close();
		
		ObjectInput oi = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
		Op op2 = serial.readObject(oi, Op.class);
		oi.close();
		
		Assert.assertEquals(op, op2);
	}
}
