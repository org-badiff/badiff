package org.badiff;

import org.badiff.io.DefaultSerialization;
import org.badiff.io.Serialization;
import org.junit.Test;

public class ByteArrayDiffTest {
	
	protected Serialization serial;
	
	public ByteArrayDiffTest() {
		this(DefaultSerialization.getInstance());
	}
	
	protected ByteArrayDiffTest(Serialization serial) {
		this.serial = serial;
		
	}

	@Test
	public void testDiff() throws Exception {
		ByteArrayDiff badiff = new ByteArrayDiff(serial);
		
		String orig = "Hello world!";
		String target = "Hellish cruel world!";
		
		byte[] diff = badiff.diff(orig.getBytes(), target.getBytes());
		
		System.out.println(diff.length);
	}

}
