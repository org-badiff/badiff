package org.badiff.kryo;

import org.badiff.ByteArrayDiffTest;
import org.badiff.io.Serialization;

public class KryoByteArrayDiffTest extends ByteArrayDiffTest {

	public KryoByteArrayDiffTest() {
		super(new KryoSerialization().stripDeletes(true));
	}

}
