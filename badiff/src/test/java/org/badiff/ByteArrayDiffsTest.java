package org.badiff;

import org.badiff.io.DefaultSerialization;
import org.badiff.io.Serialization;
import org.junit.Test;

public class ByteArrayDiffsTest {
	
	protected Serialization serial;
	
	public ByteArrayDiffsTest() {
		this(DefaultSerialization.getInstance());
	}
	
	protected ByteArrayDiffsTest(Serialization serial) {
		this.serial = serial;
		
	}

	@Test
	public void testDiff() throws Exception {
		ByteArrayDiffs badiff = new ByteArrayDiffs(serial);
		
		String orig = "Hello world!";
		String target = "Hellish cruel world!";
		
		byte[] diff = badiff.diff(orig.getBytes(), target.getBytes());
		
		System.out.println(diff.length);
	}

}
