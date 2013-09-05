package org.badiff.kryo;

import org.badiff.ByteArrayDiffsTest;

public class KryoByteArrayDiffsTest extends ByteArrayDiffsTest {

	public KryoByteArrayDiffsTest() {
		super(new KryoSerialization());
	}

}
