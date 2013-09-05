package org.badiff.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.badiff.io.NoopOutputStream;

public class Digests {
	
	public static MessageDigest sha1() {
		try {
			return MessageDigest.getInstance("SHA-1");
		} catch(NoSuchAlgorithmException nsae) {
			throw new IllegalStateException(nsae);
		}
	}

	public static byte[] hash(File file) throws IOException {
		DigestInputStream din = new DigestInputStream(new FileInputStream(file), sha1());
		Streams.copy(din, new NoopOutputStream());
		din.close();
		return din.getMessageDigest().digest();
	}
	
	private Digests() {
	}

}
