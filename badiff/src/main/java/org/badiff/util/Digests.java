package org.badiff.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digests {

	public static MessageDigest defaultDigest() {
		try {
			return MessageDigest.getInstance("SHA-1");
		} catch(NoSuchAlgorithmException nsae) {
			return null;
		}
	}
	
	public static MessageDigest digest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch(NoSuchAlgorithmException nsae) {
			return null;
		}
	}

}
