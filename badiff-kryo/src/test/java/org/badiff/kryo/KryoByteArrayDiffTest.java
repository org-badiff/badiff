package org.badiff.kryo;

import org.badiff.ByteArrayDiffTest;

public class KryoByteArrayDiffTest extends ByteArrayDiffTest {

	public KryoByteArrayDiffTest() {
		super(new KryoSerialization().stripDeletes(true));
	}

}
